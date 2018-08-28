package org.samberry.recentorder

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

private val formatErrorMessage: (Order) -> String = { order: Order ->
    "Order for amount '${order.amount}' happened too long ago (${order.timestamp})"
}

@ResponseStatus(HttpStatus.NO_CONTENT)
class OldOrderException(
    order: Order
) : RuntimeException(formatErrorMessage(order)) {
}