package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.time.ZonedDateTime

class OrderTimelineLoadTest {
    private lateinit var orderTimeline: OrderTimeline

    @Before
    fun setUp() {
        orderTimeline = OrderTimeline()
    }

    // Ignored from regular suite since it is a long running test
    // and doesn't verify any logic. Mostly useful for roughly understanding
    // the limitations of the chosen approach within a single instance.
    // Would be better to use Ab for real/concurrent performance testing
    // 50 orders yields roughly 100ms per order average.
    @Ignore
    @Test
    fun `load test`() {
        val timestamp = OrderTimestamp(
            timestamp = ZonedDateTime.parse("2018-08-23T00:30:00.000Z")
        )

        for (i in 0..999) {
            val order = Order(
                amount = OrderAmount(5.0),
                timestamp = timestamp.plusSeconds(i)
            )
            orderTimeline.addOrder(order)
        }

        assertThat(orderTimeline.getStatistics(timestamp))
            .isNotEqualTo(EMPTY_STATISTICS)

        assertThat(orderTimeline.getStatistics(timestamp.plusSeconds(500)))
            .isNotEqualTo(EMPTY_STATISTICS)
    }
}