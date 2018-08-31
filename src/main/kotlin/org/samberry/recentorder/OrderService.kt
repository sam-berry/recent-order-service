package org.samberry.recentorder

const val ORDER_DURATION_SECONDS = 30

class OrderService(
    private val orderTimeline: OrderTimeline
) {
    fun addOrder(order: Order) {
        orderTimeline.addOrder(order)
    }

    fun fetchStatistics(): OrderStatistics {
        return orderTimeline.getStatistics(OrderTimestamp.now())
    }

    fun deleteOrders() {
        orderTimeline.deleteOrders()
    }
}