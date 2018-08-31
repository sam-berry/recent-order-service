package org.samberry.recentorder

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class OrderResource(
    private val orderService: OrderService
) {
    @POST
    @Path("/orders")
    fun addOrder(order: Order) {
        orderService.addOrder(order)
    }

    @DELETE
    @Path("/orders")
    fun deleteOrders() {
        orderService.deleteOrders()
    }

    @GET
    @Path("/statistics")
    fun fetchStatistics(): OrderStatistics {
        return orderService.fetchStatistics()
    }
}