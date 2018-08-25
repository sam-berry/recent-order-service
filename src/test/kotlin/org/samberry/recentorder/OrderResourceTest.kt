package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderResourceTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @After
    fun tearDown() {
        testRestTemplate.exchange("/orders", HttpMethod.DELETE, null, Unit::class.java)
    }

    @Test
    fun `returns 201 on successful create`() {
        val order = Order(
            amount = OrderAmount(10.0),
            timestamp = OrderTimestamp.now()
        )

        val result = testRestTemplate.postForEntity("/orders", order, Unit.javaClass)

        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `returns 204 if trying to create an expired order`() {
        listOf(
            testRestTemplate.postForEntity(
                "/orders",
                Order(
                    amount = OrderAmount(10.0),
                    timestamp = OrderTimestamp.now().minusSeconds(ORDER_DURATION_SECONDS)
                ),
                Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders",
                Order(
                    amount = OrderAmount(10.0),
                    timestamp = OrderTimestamp.now().minusSeconds(ORDER_DURATION_SECONDS + 30)
                ),
                Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders",
                Order(
                    amount = OrderAmount(10.0),
                    timestamp = OrderTimestamp.now().minusSeconds(ORDER_DURATION_SECONDS * 50)
                ),
                Any::class.java
            )
        ).forEach {
            assertThat(it.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        }
    }

    @Test
    fun `returns 422 if invalid amount`() {
        listOf(
            testRestTemplate.postForEntity(
                "/orders", mapOf(
                    "amount" to "2e3ffz",
                    "timestamp" to "${OrderTimestamp.now()}"
                ), Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders", mapOf(
                    "amount" to "",
                    "timestamp" to "${OrderTimestamp.now()}"
                ), Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders", mapOf(
                    "amount" to "null",
                    "timestamp" to "${OrderTimestamp.now()}"
                ), Any::class.java
            )
        ).forEach {
            assertThat(it.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        }

    }

    @Test
    fun `returns 422 if invalid timestamp`() {
        listOf(
            testRestTemplate.postForEntity(
                "/orders", mapOf(
                    "amount" to "10.00",
                    "timestamp" to "abcd"
                ), Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders", mapOf(
                    "amount" to "10.00",
                    "timestamp" to ""
                ), Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders", mapOf(
                    "amount" to "10.00",
                    "timestamp" to "null"
                ), Any::class.java
            )
        ).forEach {
            assertThat(it.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        }
    }

    @Test
    fun `returns 400 if invalid JSON`() {
        listOf(
            testRestTemplate.postForEntity(
                "/orders", emptyMap<String, String>(), Any::class.java
            ),
            testRestTemplate.postForEntity(
                "/orders", emptyList<String>(), Any::class.java
            )
        ).forEach {
            assertThat(it.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `can delete orders`() {
        val order = Order(
            amount = OrderAmount(10.0),
            timestamp = OrderTimestamp.now()
        )

        testRestTemplate.postForEntity(
            "/orders", order, Any::class.java
        )
        testRestTemplate.postForEntity(
            "/orders", order, Any::class.java
        )

        var stats = testRestTemplate.getForEntity(
            "/statistics", OrderStatistics::class.java
        )
        assertThat(stats.body).isNotNull
        assertThat(stats.body!!.sum).isEqualTo(OrderAmount(20))

        testRestTemplate.delete("/orders")

        stats = testRestTemplate.getForEntity(
            "/statistics", OrderStatistics::class.java
        )
        assertThat(stats.body).isNotNull
        assertThat(stats.body!!).isEqualTo(EMPTY_STATISTICS)
    }

    @Test
    fun `returns a 204 after deleting`() {
        var response = testRestTemplate.exchange("/orders", HttpMethod.DELETE, null, Unit::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        val order = Order(
            amount = OrderAmount(10.0),
            timestamp = OrderTimestamp.now()
        )

        testRestTemplate.postForEntity("/orders", order, Any::class.java)
        response = testRestTemplate.exchange("/orders", HttpMethod.DELETE, null, Unit::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    fun `returns a 422 if timestamp is in the future`() {
        val order = Order(
            amount = OrderAmount(10.0),
            timestamp = OrderTimestamp.now().plusSeconds(10)
        )

        val response = testRestTemplate.postForEntity("/orders", order, Any::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
    }
}