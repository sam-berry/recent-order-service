package org.samberry.recentorder

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderTimeline {
    private val timeline: MutableMap<OrderTimestamp, MutableOrderStatistics> = ConcurrentHashMap()

    fun addOrder(order: Order) {
        val orderAmount = order.amount

        (0 until ORDER_DURATION_SECONDS).forEach {
            val timestamp = order.timestamp.plusSeconds(it).toSeconds()
            val existingStatistic = timeline[timestamp]
            if (existingStatistic == null) {
                timeline[timestamp] = MutableOrderStatistics(
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