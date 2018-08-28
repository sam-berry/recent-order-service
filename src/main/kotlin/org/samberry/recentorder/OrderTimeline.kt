package org.samberry.recentorder

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderTimeline {
    private val timeline: MutableMap<OrderTimestamp, OrderStatistics> = ConcurrentHashMap()

    fun addOrder(order: Order) {
        val orderAmount = order.amount
        val expirationTimestamp = order.timestamp.expiration()
        var currentTimestamp = order.timestamp.toSeconds()

        val timestamps = mutableListOf<OrderTimestamp>()

        while (currentTimestamp.isBefore(expirationTimestamp)) {
            timestamps.add(currentTimestamp)
            currentTimestamp = currentTimestamp.nextMoment()
        }

        timestamps
            .forEach {
                val existingStatistic = timeline[it]
                val newStatistic: OrderStatistics

                newStatistic = if (existingStatistic == null)
                    OrderStatistics(
                        sum = orderAmount,
                        avg = orderAmount,
                        max = orderAmount,
                        min = orderAmount,
                        count = 1
                    )
                else {
                    val newSum = existingStatistic.sum.plus(orderAmount)
                    val newCount = existingStatistic.count + 1

                    OrderStatistics(
                        sum = newSum,
                        avg = newSum.divide(newCount),
                        max = existingStatistic.max.orMax(orderAmount),
                        min = existingStatistic.min.orMin(orderAmount),
                        count = newCount
                    )
                }

                timeline[it] = newStatistic
            }
    }

    fun getStatistics(timestamp: OrderTimestamp): OrderStatistics {
        return timeline[timestamp.toSeconds()] ?: EMPTY_STATISTICS
    }

    fun deleteOrders() {
        timeline.clear()
    }
}