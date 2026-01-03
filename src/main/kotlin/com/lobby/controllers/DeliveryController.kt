package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.models.User
import com.lobby.services.DeliveryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/deliveries")
class DeliveryController(
    private val deliveryService: DeliveryService
) {
    @GetMapping
    fun listMyDeliveries(@CurrentUser user: User): ResponseEntity<Any> {
        val deliveries = deliveryService.listMyDeliveries(user.condominium, user.block, user.apartmentNumber)
        return ResponseEntity.ok(deliveries)
    }
}