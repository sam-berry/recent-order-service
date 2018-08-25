package org.samberry.recentorder

data class Order(
    val amount: OrderAmount,
    val timestamp: OrderTimestamp
)
