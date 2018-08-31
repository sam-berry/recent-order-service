package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.ZonedDateTime

private const val HALF_ORDER_DURATION = ORDER_DURATION_SECONDS / 2
private const val DOUBLE_ORDER_DURATION = ORDER_DURATION_SECONDS * 2

class OrderTimelineTest {
    private lateinit var orderTimeline: OrderTimeline

    @Before
    fun setUp() {
        orderTimeline = OrderTimeline()
    }

    private fun amount(value: Double): OrderAmount {
        return OrderAmount(BigDecimal.valueOf(value))
    }

    private fun timestamp(value: String): OrderTimestamp {
        return OrderTimestamp(
            timestamp = ZonedDateTime.parse(value)
        )
    }

    private fun stat(value: Double): OrderAmount {
        return OrderAmount(value)
    }

    private fun order(amount: Double, timestamp: String): Order {
        return Order(
            amount = amount(amount),
            timestamp = timestamp(timestamp)
        )
    }

    /**
     * Say a order for 5 is posted at 7:30:00.
     *
     *         7:29      7:30      7:31      7:32
     *         |----------T---------|---------|->
     *              ^     ^    ^    ^    ^
     *    sum       0     5    5    0    0
     *    avg       0     5    5    0    0
     *    max       0     5    5    0    0
     *    min       0     5    5    0    0
     *    count     0     1    1    0    0
     */
    @Test
    fun `correctly reports a single order`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        val order = order(5.0, timestamp.toString())
        val expectedStatistics = OrderStatistics(
            sum = stat(5.0),
            avg = stat(5.0),
            max = stat(5.0),
            min = stat(5.0),
            count = 1
        )

        orderTimeline.addOrder(order)

        val beforeOrder = orderTimeline
            .getStatistics(timestamp.minusSeconds(HALF_ORDER_DURATION))
        assertThat(beforeOrder).isEqualTo(EMPTY_STATISTICS)

        val timeOfOrder = orderTimeline
            .getStatistics(timestamp)
        assertThat(timeOfOrder).isEqualTo(expectedStatistics)

        val beforeOrderExpires = orderTimeline
            .getStatistics(timestamp.plusSeconds(HALF_ORDER_DURATION))
        assertThat(beforeOrderExpires).isEqualTo(expectedStatistics)

        val whenOrderExpires = orderTimeline
            .getStatistics(timestamp.plusSeconds(ORDER_DURATION_SECONDS))
        assertThat(whenOrderExpires).isEqualTo(EMPTY_STATISTICS)

        val afterOrderExpires = orderTimeline
            .getStatistics(timestamp.plusSeconds(DOUBLE_ORDER_DURATION))
        assertThat(afterOrderExpires).isEqualTo(EMPTY_STATISTICS)
    }

    @Test
    fun `reports a order inclusively based on timestamp`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        val order = order(5.0, timestamp.toString())

        orderTimeline.addOrder(order)

        val rightBeforeCreation = timestamp.previousMoment()
        assertThat(orderTimeline.getStatistics(rightBeforeCreation))
            .isEqualTo(EMPTY_STATISTICS)

        assertThat(orderTimeline.getStatistics(timestamp))
            .isNotEqualTo(EMPTY_STATISTICS)

        val rightAfterCreation = timestamp.nextMoment()
        assertThat(orderTimeline.getStatistics(rightAfterCreation))
            .isNotEqualTo(EMPTY_STATISTICS)
    }

    /**
     * Say the duration is 60s and the posted timestamp is "07:30:00". Including
     * "07:30:00" and "07:31:00" would include an extra moment in time, or 61s.
     * Instead, "07:30:59" should be the stopping point.
     */
    @Test
    fun `order duration has an exclusive end`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        val order = order(5.0, timestamp.toString())

        orderTimeline.addOrder(order)

        val rightBeforeDeletion = timestamp.plusSeconds(ORDER_DURATION_SECONDS).previousMoment()
        assertThat(orderTimeline.getStatistics(rightBeforeDeletion))
            .isNotEqualTo(EMPTY_STATISTICS)

        val deletionTime = timestamp.plusSeconds(ORDER_DURATION_SECONDS)
        assertThat(orderTimeline.getStatistics(deletionTime))
            .isEqualTo(EMPTY_STATISTICS)

        val rightAfterDeletion = timestamp.plusSeconds(ORDER_DURATION_SECONDS).nextMoment()
        assertThat(orderTimeline.getStatistics(rightAfterDeletion))
            .isEqualTo(EMPTY_STATISTICS)
    }

    /**
     * Say two orders for 5 are posted at 7:30:00.
     *
     *         7:29      7:30      7:31      7:32
     *         |----------T---------|---------|->
     *              ^     ^    ^    ^    ^
     *    sum       0     5    5    0    0
     *    avg       0     5    5    0    0
     *    max       0     5    5    0    0
     *    min       0     5    5    0    0
     *    count     0     1    1    0    0
     */
    @Test
    fun `correctly reports two orders that have the same timestamp`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        val order = order(5.0, timestamp.toString())
        val expectedStatistics = OrderStatistics(
            sum = stat(10.0),
            avg = stat(5.0),
            max = stat(5.0),
            min = stat(5.0),
            count = 2
        )

        orderTimeline.addOrder(order)
        orderTimeline.addOrder(order)

        val beforeOrder = orderTimeline
            .getStatistics(timestamp.minusSeconds(HALF_ORDER_DURATION))
        assertThat(beforeOrder).isEqualTo(EMPTY_STATISTICS)

        val timeOfOrder = orderTimeline
            .getStatistics(timestamp)
        assertThat(timeOfOrder).isEqualTo(expectedStatistics)

        val beforeOrderExpires = orderTimeline
            .getStatistics(timestamp.plusSeconds(HALF_ORDER_DURATION))
        assertThat(beforeOrderExpires).isEqualTo(expectedStatistics)

        val whenOrderExpires = orderTimeline
            .getStatistics(timestamp.plusSeconds(ORDER_DURATION_SECONDS))
        assertThat(whenOrderExpires).isEqualTo(EMPTY_STATISTICS)

        val afterOrderExpires = orderTimeline
            .getStatistics(timestamp.plusSeconds(DOUBLE_ORDER_DURATION))
        assertThat(afterOrderExpires).isEqualTo(EMPTY_STATISTICS)
    }

    @Test
    fun `correctly reports two overlapping orders`() {
        val timestampA = timestamp("2018-08-23T07:30:00.000Z")
        val orderA = order(5.0, timestampA.toString())
        val timestampB = timestampA.plusSeconds(ORDER_DURATION_SECONDS / 2)
        val orderB = order(7.0, timestampB.toString())

        orderTimeline.addOrder(orderA)
        orderTimeline.addOrder(orderB)

        val aStatistics = OrderStatistics(
            sum = stat(5.0),
            avg = stat(5.0),
            max = stat(5.0),
            min = stat(5.0),
            count = 1
        )
        val bStatistics = OrderStatistics(
            sum = stat(7.0),
            avg = stat(7.0),
            max = stat(7.0),
            min = stat(7.0),
            count = 1
        )
        val aAndBStatistics = OrderStatistics(
            sum = stat(12.0),
            avg = stat(6.0),
            max = stat(7.0),
            min = stat(5.0),
            count = 2
        )

        assertThat(orderTimeline.getStatistics(timestampA.previousMoment()))
            .isEqualTo(EMPTY_STATISTICS)
        assertThat(orderTimeline.getStatistics(timestampA))
            .isEqualTo(aStatistics)
        assertThat(orderTimeline.getStatistics(timestampB))
            .isEqualTo(aAndBStatistics)
        assertThat(orderTimeline.getStatistics(timestampB.plusSeconds(ORDER_DURATION_SECONDS - 1)))
            .isEqualTo(bStatistics)
        assertThat(orderTimeline.getStatistics(timestampB.plusSeconds(ORDER_DURATION_SECONDS)))
            .isEqualTo(EMPTY_STATISTICS)
    }

    /**
     * Say a order for 5 is posted at 7:30:00, then a
     * order for 7 is posted at 7:31:00.
     *
     *         7:29      7:30      7:31      7:32
     *         |----------T---------T---------|->
     *              ^     ^    ^    ^    ^
     *    sum       0     5    5    7    7
     *    avg       0     5    5    7    7
     *    max       0     5    5    7    7
     *    min       0     5    5    7    7
     *    count     0     1    1    1    1
     */
    @Test
    fun `correctly reports two adjacent orders`() {
        val timestampA = timestamp("2018-08-23T07:30:00.000Z")
        val orderA = order(5.0, timestampA.toString())
        val timestampB = timestampA.plusSeconds(ORDER_DURATION_SECONDS)
        val orderB = order(7.0, timestampB.toString())

        orderTimeline.addOrder(orderA)
        orderTimeline.addOrder(orderB)

        assertThat(orderTimeline.getStatistics(timestampA.previousMoment())).isEqualTo(EMPTY_STATISTICS)

        val aStatistics = OrderStatistics(
            sum = stat(5.0),
            avg = stat(5.0),
            max = stat(5.0),
            min = stat(5.0),
            count = 1
        )
        val bStatistics = OrderStatistics(
            sum = stat(7.0),
            avg = stat(7.0),
            max = stat(7.0),
            min = stat(7.0),
            count = 1
        )

        assertThat(orderTimeline.getStatistics(timestampA)).isEqualTo(aStatistics)
        assertThat(orderTimeline.getStatistics(timestampA.nextMoment())).isEqualTo(aStatistics)
        assertThat(orderTimeline.getStatistics(timestampB)).isEqualTo(bStatistics)
        assertThat(orderTimeline.getStatistics(timestampB.nextMoment())).isEqualTo(bStatistics)
        assertThat(orderTimeline.getStatistics(timestampB.expiration())).isEqualTo(EMPTY_STATISTICS)
    }

    /**
     * Say a order for 5 is posted at 7:30:00, then a
     * order for 7 is posted at 7:31:30.
     *
     *         7:29      7:30      7:31      7:32
     *         |----------T---------|----T----|->
     *              ^     ^    ^    ^    ^
     *    sum       0     5    5    0    7
     *    avg       0     5    5    0    7
     *    max       0     5    5    0    7
     *    min       0     5    5    0    7
     *    count     0     1    1    0    1
     */
    @Test
    fun `correctly reports two not-overlapping orders`() {
        val timestampA = timestamp("2018-08-23T07:30:00.000Z")
        val orderA = order(5.0, timestampA.toString())
        val timestampB = timestamp("2018-08-23T07:31:30.000Z")
        val orderB = order(7.0, timestampB.toString())

        orderTimeline.addOrder(orderA)
        orderTimeline.addOrder(orderB)


        val timestampAStatistics = OrderStatistics(
            sum = stat(5.0),
            avg = stat(5.0),
            max = stat(5.0),
            min = stat(5.0),
            count = 1
        )

        assertThat(orderTimeline.getStatistics(timestampA.previousMoment())).isEqualTo(EMPTY_STATISTICS)
        assertThat(orderTimeline.getStatistics(timestampA)).isEqualTo(timestampAStatistics)
        assertThat(orderTimeline.getStatistics(timestampA.nextMoment())).isEqualTo(timestampAStatistics)
        assertThat(orderTimeline.getStatistics(timestampA.expiration())).isEqualTo(EMPTY_STATISTICS)

        val timestampBStatistics = OrderStatistics(
            sum = stat(7.0),
            avg = stat(7.0),
            max = stat(7.0),
            min = stat(7.0),
            count = 1
        )
        assertThat(orderTimeline.getStatistics(timestampB.previousMoment())).isEqualTo(EMPTY_STATISTICS)
        assertThat(orderTimeline.getStatistics(timestampB)).isEqualTo(timestampBStatistics)
        assertThat(orderTimeline.getStatistics(timestampB.nextMoment())).isEqualTo(timestampBStatistics)
        assertThat(orderTimeline.getStatistics(timestampB.expiration())).isEqualTo(EMPTY_STATISTICS)
    }

    @Test
    fun `can delete all orders`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        val order = order(5.0, timestamp.toString())

        orderTimeline.addOrder(order)
        assertThat(orderTimeline.getStatistics(timestamp)).isNotEqualTo(EMPTY_STATISTICS)

        orderTimeline.deleteOrders()
        assertThat(orderTimeline.getStatistics(timestamp)).isEqualTo(EMPTY_STATISTICS)

        // make sure the store is not destroyed, try adding again after deleting
        orderTimeline.addOrder(order)
        assertThat(orderTimeline.getStatistics(timestamp)).isNotEqualTo(EMPTY_STATISTICS)

        orderTimeline.deleteOrders()
        assertThat(orderTimeline.getStatistics(timestamp)).isEqualTo(EMPTY_STATISTICS)
    }

    @Test
    fun `can get statistics for multiple orders`() {
        listOf(
            order(10.0, OrderTimestamp.now().toString()),
            order(599.11, OrderTimestamp.now().toString()),
            order(0.33, OrderTimestamp.now().toString())
        ).forEach {
            orderTimeline.addOrder(it)
        }

        val result = orderTimeline.getStatistics(OrderTimestamp.now())

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

    @Test
    fun `can handle decimal division`() {
        listOf(
            order(5.0, "2018-08-25T01:39:25.247Z"),
            order(3.0, "2018-08-25T01:39:35.329Z"),
            order(3.0, "2018-08-25T01:39:44.623Z")
        ).forEach {
            orderTimeline.addOrder(it)
        }

        val result = orderTimeline.getStatistics(OrderTimestamp.fromString("2018-08-25T01:39:44.623Z"))

        assertThat(result).isEqualTo(
            OrderStatistics(
                sum = stat(11.00),
                avg = stat(3.67),
                max = stat(5.00),
                min = stat(3.00),
                count = 3
            )
        )
    }
}