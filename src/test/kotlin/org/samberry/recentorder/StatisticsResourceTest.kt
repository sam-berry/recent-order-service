package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatisticsResourceTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    private fun stat(value: Double): OrderAmount {
        return OrderAmount(value)
    }

    @After
    fun tearDown() {
        testRestTemplate.exchange("/orders", HttpMethod.DELETE, null, Unit::class.java)
    }

    @Test
    fun `can get statistics for a single order`() {
        testRestTemplate.postForEntity(
            "/orders", Order(
                amount = OrderAmount(10.0),
                timestamp = OrderTimestamp.now()
            ), Any::class.java
        )

        val result = testRestTemplate.getForEntity("/statistics", OrderStatistics::class.java).body!!

        assertThat(result).isEqualTo(
            OrderStatistics(
                sum = stat(10.0),
                avg = stat(10.0),
                max = stat(10.0),
                min = stat(10.0),
                count = 1
            )
        )
    }

    @Test
    fun `can get statistics for multiple orders`() {
        listOf(
            Order(
                amount = OrderAmount(10.0),
                timestamp = OrderTimestamp.now()
            ),
            Order(
                amount = OrderAmount(599.11),
                timestamp = OrderTimestamp.now()
            ),
            Order(
                amount = OrderAmount(0.33),
                timestamp = OrderTimestamp.now()
            )
        ).forEach {
            testRestTemplate.postForEntity("/orders", it, Any::class.java)
        }

        val result = testRestTemplate.getForEntity("/statistics", OrderStatistics::class.java).body!!

        assertThat(result).isEqualTo(
            OrderStatistics(
                sum = stat(609.44),
                avg = stat(203.15),
                max = stat(599.11),
                min = stat(0.33),
                count = 3
            )
        )
    }
}