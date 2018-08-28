package org.samberry.recentorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OrderAmountTest {
    @Test
    fun `can add an amount`() {
        val result = OrderAmount(2.0)
            .plus(OrderAmount(7.2))
        assertThat(result).isEqualTo(OrderAmount(9.2))
    }

    @Test
    fun `can divide a whole amount`() {
        val result = OrderAmount(21.0).divide(7)
        assertThat(result).isEqualTo(OrderAmount(3.0))
    }

    @Test
    fun `can divide a partial number`() {
        val result = OrderAmount(31.035).divide(3)
        assertThat(result).isEqualTo(OrderAmount(10.35))
    }

    @Test
    fun `chooses the left as max when it's bigger`() {
        val result = OrderAmount(3.0)
            .orMax(OrderAmount(1.0))
        assertThat(result).isEqualTo(OrderAmount(3.0))
    }

    @Test
    fun `chooses the right as max when it's bigger`() {
        val result = OrderAmount(3.0)
            .orMax(OrderAmount(10.0))
        assertThat(result).isEqualTo(OrderAmount(10.0))
    }

    @Test
    fun `chooses the only choice if both max arguments are equal`() {
        val result = OrderAmount(3.0)
            .orMax(OrderAmount(3.0))
        assertThat(result).isEqualTo(OrderAmount(3.0))
    }

    @Test
    fun `chooses the left as min when it's smaller`() {
        val result = OrderAmount(1.0)
            .orMin(OrderAmount(3.0))
        assertThat(result).isEqualTo(OrderAmount(1.0))
    }

    @Test
    fun `chooses the right as min when it's smaller`() {
        val result = OrderAmount(3.0)
            .orMin(OrderAmount(1.0))
        assertThat(result).isEqualTo(OrderAmount(1.0))
    }

    @Test
    fun `chooses the only choice if both min arguments are equal`() {
        val result = OrderAmount(3.0)
            .orMax(OrderAmount(3.0))
        assertThat(result).isEqualTo(OrderAmount(3.0))
    }

    @Test
    fun `rounds 4 down to 2 decimal places when formatting`() {
        val result = OrderAmount(31.034)
        assertThat(result.format()).isEqualTo(OrderAmount(31.03).format())
    }

    @Test
    fun `rounds 5 up to 2 decimal places when formatting`() {
        val result = OrderAmount(31.035)
        assertThat(result.format()).isEqualTo(OrderAmount(31.04).format())
    }

    @Test
    fun `formats to two decimal places when there is no decimals`() {
        assertThat(OrderAmount(3.0).format()).isEqualTo("3.00")
    }

    @Test
    fun `formats to two decimal places when there is only one decimal`() {
        assertThat(OrderAmount(3.1).format()).isEqualTo("3.10")
    }

    @Test
    fun `translates a whole number string`() {
        assertThat(OrderAmount.fromString("10")).isEqualTo(
            OrderAmount(
                10
            )
        )
    }

    @Test
    fun `translates a 1 decimal string`() {
        assertThat(OrderAmount.fromString("10.3")).isEqualTo(
            OrderAmount(
                10.3
            )
        )
    }

    @Test
    fun `translates a 2 decimal string`() {
        assertThat(OrderAmount.fromString("10.34")).isEqualTo(
            OrderAmount(
                10.34
            )
        )
    }

    @Test
    fun `translates a 5 decimal string`() {
        assertThat(OrderAmount.fromString("10.34121")).isEqualTo(
            OrderAmount(
                10.34121
            )
        )
        assertThat(OrderAmount.fromString("10.34121").format()).isEqualTo("10.34")
    }

    @Test(expected = InvalidInputException::class)
    fun `throws invalid input exception if empty string provided`() {
        OrderAmount.fromString("")
    }

    @Test(expected = InvalidInputException::class)
    fun `throws invalid input exception if character string provided`() {
        OrderAmount.fromString("abc")
    }

    @Test(expected = InvalidInputException::class)
    fun `throws invalid input exception if whitespace string provided`() {
        OrderAmount.fromString("   ")
    }

    @Test(expected = InvalidInputException::class)
    fun `throws invalid input exception if whitespace and numbers provided`() {
        OrderAmount.fromString("1 ")
    }
}