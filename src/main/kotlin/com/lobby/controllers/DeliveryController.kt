package com.lobby.controllers

import com.lobby.services.DeliveryService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/deliveries")
class DeliveryController(
    private val deliveryService: DeliveryService
) {
    @GetMapping
    fun listMyDeliveries(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<Any> {
        return deliveryService.listMyDeliveries(userDetails.username)
    }
}