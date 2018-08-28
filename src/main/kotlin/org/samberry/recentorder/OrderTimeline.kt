package org.samberry.recentorder

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderTimeline {
    private val timeline: MutableMap<OrderTimestamp, MutableOrderStatistics> = ConcurrentHashMap()

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

                if (existingStatistic == null) {
                    timeline[it] = MutableOrderStatistics(
                        sum = orderAmount,
                        avg = orderAmount,
                        max = orderAmount,
                        min = orderAmount,
                        count = 1
                    )
                } else {
                    existingStatistic.addOrder(orderAmount)
                }
            }
    }

    fun getStatistics(timestamp: OrderTimestamp): OrderStatistics {
        return timeline[timestamp.toSeconds()]?.toImmutable() ?: EMPTY_STATISTICS
    }

    fun deleteOrders() {
        timeline.clear()
    }
}