package org.samberry.recentorder

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderResource(
    private val orderService: OrderService
) {
    @PostMapping("/orders")
    fun addOrder(@RequestBody order: Order): ResponseEntity<Unit> {
        orderService.addOrder(order)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @DeleteMapping("/orders")
    fun deleteOrders(): ResponseEntity<Unit> {
        orderService.deleteOrders()

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @ResponseBody
    @GetMapping("/statistics")
    fun fetchStatistics(): OrderStatistics {
        return orderService.fetchStatistics()
    }
}