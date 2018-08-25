package org.samberry.recentorder

val EMPTY_STATISTICS = OrderStatistics(
    sum = OrderAmount(0.0),
    avg = OrderAmount(0.0),
    max = OrderAmount(0.0),
    min = OrderAmount(0.0),
    count = 0
)

data class OrderStatistics(
    val sum: OrderAmount,
    val avg: OrderAmount,
    val max: OrderAmount,
    val min: OrderAmount,
    val count: Long
)
