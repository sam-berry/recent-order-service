package org.samberry.recentorder

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.math.BigDecimal
import java.math.RoundingMode

private val ROUNDING_MODE = RoundingMode.HALF_UP

data class OrderAmount(
    private val amount: BigDecimal
) {
    constructor(amount: Int) : this(amount.toBigDecimal())
    constructor(amount: Double) : this(amount.toBigDecimal())

    fun plus(other: OrderAmount): OrderAmount {
        return OrderAmount(amount.plus(other.amount))
    }

    fun divide(other: Long): OrderAmount {
        return OrderAmount(
            amount.divide(
                other.toBigDecimal(),
                2,
                ROUNDING_MODE
            )
        )
    }

    fun orMax(other: OrderAmount): OrderAmount {
        return OrderAmount(maxOf(amount, other.amount))
    }

    fun orMin(other: OrderAmount): OrderAmount {
        return OrderAmount(minOf(amount, other.amount))
    }

    @JsonValue
    fun format(): String {
        return amount.setScale(2, ROUNDING_MODE).toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is OrderAmount) return false
        return this.amount.compareTo(other.amount) == 0
    }

    override fun hashCode(): Int {
        return amount.hashCode()
    }

    companion object {
        val ZERO = OrderAmount(0)

        @JvmStatic
        @JsonCreator
        fun fromString(value: String): OrderAmount {
            return OrderAmount(value.toBigDecimal())
        }
    }
}
