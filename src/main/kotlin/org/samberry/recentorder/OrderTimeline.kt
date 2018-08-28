package org.samberry.recentorder

import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

private const val NUMBER_OF_WORKERS = 10
private const val MILLISECONDS_IN_SECOND = 1000
private const val TOTAL_NUMBER_OF_TIMESTAMPS = ORDER_DURATION_SECONDS * MILLISECONDS_IN_SECOND
private const val TIMESTAMPS_PER_WORKER = TOTAL_NUMBER_OF_TIMESTAMPS.div(NUMBER_OF_WORKERS)

@Service
class OrderTimeline {
    private val timeline: MutableMap<OrderTimestamp, MutableOrderStatistics> = ConcurrentHashMap()
    private val executorService = Executors.newFixedThreadPool(NUMBER_OF_WORKERS)

    fun addOrder(order: Order) {
        val orderAmount = order.amount
        val expirationTimestamp = order.timestamp.expiration()
        var currentTimestamp = order.timestamp

        val timestamps = mutableListOf<OrderTimestamp>()

        while (currentTimestamp.isBefore(expirationTimestamp)) {
            timestamps.add(currentTimestamp)
            currentTimestamp = currentTimestamp.nextMoment()
        }

        val workers = mutableListOf<Callable<Unit>>()

        timestamps.chunked(TIMESTAMPS_PER_WORKER).forEach { timestampsForWorker ->
            workers.add(Callable {
                timestampsForWorker
                    .forEach {
                        val existingStatistic = timeline[it]
                        if (existingStatistic == null)
                            timeline[it] = MutableOrderStatistics(
                                sum = orderAmount,
                                avg = orderAmount,
                                max = orderAmount,
                                min = orderAmount,
                                count = 1
                            )
                        else
                            existingStatistic.addOrder(orderAmount)
                    }
            })
        }

        executorService.invokeAll(workers)
    }

    fun getStatistics(timestamp: OrderTimestamp): OrderStatistics {
        return timeline[timestamp]?.toImmutable() ?: EMPTY_STATISTICS
    }

    fun deleteOrders() {
        timeline.clear()
    }
}