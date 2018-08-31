package org.samberry.recentorder

import org.springframework.stereotype.Service

const val ORDER_DURATION_SECONDS = 30

@Service
class OrderService(
    private val orderTimeline: OrderTimeline
) {
    fun addOrder(order: Order) {
        validateOrder(order)
        orderTimeline.addOrder(order)
    }

    private fun validateOrder(order: Order) {
        if (order.timestamp.expiration().isOnOrBeforeNow())
            throw OldOrderException(order)

        if (OrderTimestamp.now().isBefore(order.timestamp))
            throw InvalidInputException("order timestamp", order)
    }

    fun fetchStatistics(): OrderStatistics {
        return orderTimeline.getStatistics(OrderTimestamp.now())
    }

    fun deleteOrders() {
        orderTimeline.deleteOrders()
    }
}