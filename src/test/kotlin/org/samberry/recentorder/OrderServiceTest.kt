package org.samberry.recentorder

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class OrderServiceTest {
    private lateinit var orderService: OrderService

    private lateinit var orderTimeline: OrderTimeline

    @Before
    fun setUp() {
        orderTimeline = mock(OrderTimeline::class.java)

        orderService = OrderService(
            orderTimeline = orderTimeline
        )
    }

    @Test(expected = OldOrderException::class)
    fun `throws exception if duration is passed`() {
        orderService.addOrder(
            Order(
                amount = OrderAmount.ZERO,
                timestamp = OrderTimestamp.now().minusSeconds(ORDER_DURATION_SECONDS)
            )
        )
    }

    @Test(expected = InvalidInputException::class)
    fun `throws exception if timestamp in the future`() {
        orderService.addOrder(
            Order(
                amount = OrderAmount.ZERO,
                timestamp = OrderTimestamp.now().plusSeconds(100)
            )
        )
    }

    @Test
    fun `adds a order if it is not expired`() {
        val order = Order(
            amount = OrderAmount.ZERO,
            timestamp = OrderTimestamp.now()
        )

        orderService.addOrder(order)

        Mockito.verify(orderTimeline).addOrder(order)
    }
}