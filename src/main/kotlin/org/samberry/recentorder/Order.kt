package org.samberry.recentorder

import com.fasterxml.jackson.annotation.JsonCreator

data class Order(
    val amount: OrderAmount,
    val timestamp: OrderTimestamp
)
