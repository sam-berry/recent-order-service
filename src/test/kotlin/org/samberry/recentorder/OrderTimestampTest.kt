package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.ZonedDateTime

class OrderTimestampTest {
    private fun timestamp(value: String): OrderTimestamp {
        return OrderTimestamp(
            timestamp = ZonedDateTime.parse(value)
        )
    }

    @Test
    fun `increments 1 second to get to the next moment`() {
        val timestamp = timestamp("2018-08-23T07:30:00.922Z")
        assertThat(timestamp.nextMoment().toString()).isEqualTo("2018-08-23T07:30:01.000Z")
    }

    @Test
    fun `decrements 1 second to get to the previous moment`() {
        val timestamp = timestamp("2018-08-23T07:30:00.283Z")
        assertThat(timestamp.previousMoment().toString()).isEqualTo("2018-08-23T07:29:59.000Z")
    }

    @Test
    fun `sets expiration 60s out`() {
        val timestamp = timestamp("2018-08-23T07:30:04.871Z")
        assertThat(timestamp.expiration().toString()).isEqualTo("2018-08-23T07:31:04.000Z")
    }

    @Test
    fun `can add seconds`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        assertThat(timestamp.plusSeconds(3).toString()).isEqualTo("2018-08-23T07:30:03.000Z")
    }

    @Test
    fun `can subtract seconds`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        assertThat(timestamp.minusSeconds(3).toString()).isEqualTo("2018-08-23T07:29:57.000Z")
    }

    @Test
    fun `can add millis`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        assertThat(timestamp.plusMillis(10).toString()).isEqualTo("2018-08-23T07:30:00.010Z")
    }

    @Test
    fun `can subtract millis`() {
        val timestamp = timestamp("2018-08-23T07:30:00.000Z")
        assertThat(timestamp.minusMillis(10).toString()).isEqualTo("2018-08-23T07:29:59.990Z")
    }

    @Test
    fun `determines if an other timestamp is the same`() {
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isSame(timestamp("2018-08-23T07:30:00.000Z"))).isFalse()
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isSame(timestamp("2018-08-23T07:30:00.001Z"))).isTrue()
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isSame(timestamp("2018-08-23T07:30:00.002Z"))).isFalse()
    }

    @Test
    fun `determines if an other timestamp is before`() {
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isBefore(timestamp("2018-08-23T07:30:00.000Z"))).isFalse()
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isBefore(timestamp("2018-08-23T07:30:00.001Z"))).isFalse()
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isBefore(timestamp("2018-08-23T07:30:00.002Z"))).isTrue()
    }

    @Test
    fun `determines if an other timestamp is on or before`() {
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isOnOrBefore(timestamp("2018-08-23T07:30:00.000Z"))).isFalse()
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isOnOrBefore(timestamp("2018-08-23T07:30:00.001Z"))).isTrue()
        assertThat(timestamp("2018-08-23T07:30:00.001Z").isOnOrBefore(timestamp("2018-08-23T07:30:00.002Z"))).isTrue()
    }

    @Test
    fun `can translate a ISO-8601 with 'T' and 'Z'`() {
        assertThat(OrderTimestamp.fromString("2018-08-23T07:30:00.000Z").toString())
            .isEqualTo("2018-08-23T07:30:00.000Z")
    }
}