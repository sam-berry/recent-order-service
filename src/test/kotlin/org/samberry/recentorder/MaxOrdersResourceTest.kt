package org.samberry.recentorder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.dropwizard.jackson.Jackson
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

class MaxOrdersResourceTest {
    private val numberOfThreads = 10
    private val numberOfOrders = 2_000_000

    private lateinit var amounts: List<Double>
    private lateinit var executorService: ExecutorService
    private lateinit var webTarget: WebTarget
    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp() {
        amounts = listOf(22.31, 22.11, 10.1, 0.02, 0.03, 155.2, 7.73)
        executorService = Executors.newFixedThreadPool(numberOfThreads)
        webTarget = ClientBuilder.newClient().target("http://localhost:8081/api")
        objectMapper = Jackson.newObjectMapper().registerModule(KotlinModule())
        clearOrders()
    }

    @After
    fun tearDown() {
        clearOrders()
    }

    private fun clearOrders() {
        webTarget.path("/orders").request().delete()
    }

    private fun addAmounts(left: Double, right: Double): Double {
        return BigDecimal(left).plus(BigDecimal(right))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    @Test(timeout = (ORDER_DURATION_SECONDS + 3) * 1000L)
    fun `can process the maximum number of orders fast enough concurrently`() {
        val workers = (1..numberOfOrders)
            .chunked(numberOfOrders / numberOfThreads)
            .map { jobsForWorker ->
                Callable {
                    Thread.currentThread().id to jobsForWorker
                        .map { amounts[it % amounts.size] }
                        .map {
                            webTarget.path("/orders")
                                .request()
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .post(Entity.json(Order(OrderAmount(it), OrderTimestamp.now())))
                            it
                        }
                        .reduce { total, amount -> addAmounts(total, amount) }
                }
            }

        val results = executorService.invokeAll(workers)
            .map { it.get() }

        val actualNumberOfThreads = results.map { it.first }.toHashSet().size
        if (actualNumberOfThreads != numberOfThreads)
            throw RuntimeException("$actualNumberOfThreads threads used when $numberOfThreads was desired")

        val totalAmount = results
            .map { it.second }
            .reduce { total, amount -> addAmounts(total, amount) }

        val statsResponse = webTarget.path("/statistics").request().get()
        val stats = objectMapper.readValue(statsResponse.entity as InputStream, OrderStatistics::class.java)

        assertThat(stats.sum).isEqualTo(OrderAmount(totalAmount))
    }
}