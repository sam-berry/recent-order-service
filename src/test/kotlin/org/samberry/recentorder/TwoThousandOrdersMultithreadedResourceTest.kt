package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val NUMBER_OF_THREADS = 4

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TwoThousandOrdersMultithreadedResourceTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    private lateinit var amounts: List<Double>
    private lateinit var executorService: ExecutorService

    @Before
    fun setUp() {
        amounts = listOf(22.31, 22.11, 10.1, 0.02, 0.03, 155.2, 7.73)
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
    }

    @After
    fun tearDown() {
        testRestTemplate.exchange("/orders", HttpMethod.DELETE, null, Unit::class.java)
    }

    private fun addAmounts(left: Double, right: Double): Double {
        return BigDecimal(left).plus(BigDecimal(right))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    /**
     * 1. Post 2000 orders across the specified number of threads
     * 2. Retrieve statistics
     * 3. Expect all 2000 orders to be reported
     *
     * This test verifies that 2000 orders can be posted and processed before they start to expire. This
     * test also verifies the concurrent stability of the system.
     */
    @Test
    fun `can process 2000 orders from concurrent threads fast enough to provide statistics immediately after`() {
        val totalNumberOfOrders = 2000
        val workers = (1..totalNumberOfOrders)
            .chunked(totalNumberOfOrders / NUMBER_OF_THREADS)
            .map { jobsForWorker ->
                Callable {
                    Thread.currentThread().id to jobsForWorker
                        .map { amounts[it % amounts.size] }
                        .map {
                            testRestTemplate.postForEntity(
                                "/orders", Order(
                                    amount = OrderAmount(it),
                                    timestamp = OrderTimestamp.now()
                                ), Unit.javaClass
                            )
                            it
                        }
                        .reduce { total, amount -> addAmounts(total, amount) }
                }
            }

        val results = executorService.invokeAll(workers)
            .map { it.get() }

        val actualNumberOfThreads = results.map { it.first }.toHashSet().size
        if (actualNumberOfThreads != NUMBER_OF_THREADS)
            throw RuntimeException("$actualNumberOfThreads used")

        val totalAmount = results
            .map { it.second }
            .reduce { total, amount -> addAmounts(total, amount) }


        val stats = testRestTemplate.getForEntity(
            "/statistics", OrderStatistics::class.java
        )

        assertThat(stats.body!!.sum).isEqualTo(OrderAmount(totalAmount))
    }
}