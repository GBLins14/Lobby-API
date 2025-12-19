package com.lobby.controllers

import com.lobby.dto.CreateDeliveryDto
import com.lobby.services.DoormanService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doorman/deliveries")
class DoormanController(
    private val doormanService: DoormanService
) {
    @GetMapping
    fun getAllDeliveries(): ResponseEntity<Any> {
        return doormanService.getAllDeliveries()
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateDeliveryDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Any> {
        return doormanService.registerDelivery(request, userDetails.username)
    }

    @GetMapping("/{trackingCode}")
    fun listTrackingCode(@PathVariable trackingCode: String): ResponseEntity<Any> {
        return doormanService.getDeliveryByCode(trackingCode)
    }

    @PutMapping("/{trackingCode}/confirm")
    fun confirmReceipt(@PathVariable trackingCode: String): ResponseEntity<Any> {
        return doormanService.confirmDelivery(trackingCode)
    }
}