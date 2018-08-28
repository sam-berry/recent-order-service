package org.samberry.recentorder

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private const val NANOSECONDS_IN_A_MILLISECOND = 1_000_000

private val DATE_FORMAT = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    .withZone(ZoneId.of("UTC"))

data class OrderTimestamp(
    private val timestamp: ZonedDateTime
) {
    fun nextMoment(): OrderTimestamp {
        return this.plusMillis(1)
    }

    fun previousMoment(): OrderTimestamp {
        return this.minusMillis(1)
    }

    fun expiration(): OrderTimestamp {
        return OrderTimestamp(
            timestamp = timestamp.plusSeconds(ORDER_DURATION_SECONDS.toLong())
        )
    }

    fun plusSeconds(seconds: Int): OrderTimestamp {
        return OrderTimestamp(
            timestamp = this.timestamp.plusSeconds(seconds.toLong())
        )
    }

    fun minusSeconds(seconds: Int): OrderTimestamp {
        return OrderTimestamp(
            timestamp = this.timestamp.minusSeconds(seconds.toLong())
        )
    }

    fun plusMillis(millis: Long): OrderTimestamp {
        return OrderTimestamp(
            timestamp = this.timestamp.plusNanos(millis * NANOSECONDS_IN_A_MILLISECOND)
        )
    }

    fun minusMillis(millis: Long): OrderTimestamp {
        return OrderTimestamp(
            timestamp = this.timestamp.minusNanos(millis * NANOSECONDS_IN_A_MILLISECOND)
        )
    }

    fun isSame(other: OrderTimestamp): Boolean {
        return this.timestamp.isEqual(other.timestamp)
    }

    fun isBefore(other: OrderTimestamp): Boolean {
        return this.timestamp.isBefore(other.timestamp)
    }

    fun isOnOrBefore(other: OrderTimestamp): Boolean {
        return this.isSame(other) || this.isBefore(other)
    }

    fun isOnOrBeforeNow(): Boolean {
        return isOnOrBefore(now())
    }

    @JsonValue
    override fun toString(): String {
        return DATE_FORMAT.format(timestamp)
    }

    override fun equals(other: Any?): Boolean {
        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    companion object {
        fun now(): OrderTimestamp {
            return OrderTimestamp(ZonedDateTime.now())
        }

        @JvmStatic
        @JsonCreator
        fun fromString(value: String): OrderTimestamp {
            try {
                return OrderTimestamp(ZonedDateTime.parse(value))
            } catch (e: DateTimeParseException) {
                throw InvalidInputException("order timestamp", value)
            }
        }
    }
}