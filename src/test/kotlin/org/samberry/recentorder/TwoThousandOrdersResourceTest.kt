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

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TwoThousandOrdersResourceTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    private lateinit var amounts: List<Double>

    @Before
    fun setUp() {
        amounts = listOf(22.31, 22.11, 10.1, 0.02, 0.03, 155.2, 7.73)
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
     * 1. Post 2000 orders starting now
     * 2. Retrieve statistics
     * 3. Expect all 2000 orders to be reported
     *
     * This test verifies that 2000 orders can be posted and processed before they start to expire.
     */
    @Test
    fun `can process 2000 sequential orders fast enough to provide statistics immediately after`() {
        val total = (1..2000).map { amounts[it % amounts.size] }
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

        val stats = testRestTemplate.getForEntity(
            "/statistics", OrderStatistics::class.java
        )

        assertThat(stats.body!!.sum).isEqualTo(OrderAmount(total))
    }
}