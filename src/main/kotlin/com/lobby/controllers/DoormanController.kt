package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.CreateDeliveryDto
import com.lobby.extensions.success
import com.lobby.models.User
import com.lobby.services.DoormanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/doorman/deliveries")
class DoormanController(
    private val doormanService: DoormanService
) {
    @GetMapping
    fun getAllDeliveries(@CurrentUser user: User): ResponseEntity<Any> {
        val deliveries = doormanService.getAllDeliveries(user.condominium!!)
        return ResponseEntity.ok(deliveries)
    }

    @PostMapping
    fun createDelivery(@RequestBody request: CreateDeliveryDto, @CurrentUser user: User): ResponseEntity<Any> {
        doormanService.registerDelivery(request, user.condominium!!, user.username)
        return ResponseEntity.ok().success("Encomenda criada com sucesso!")
    }

    @GetMapping("/{trackingCode}")
    fun listTrackingCode(@CurrentUser user: User, @PathVariable trackingCode: String): ResponseEntity<Any> {
        val delivery = doormanService.getDeliveryByCode(user.condominium!!, trackingCode)
        return ResponseEntity.ok(delivery)
    }

    @PutMapping("/{trackingCode}/confirm")
    fun confirmReceipt(@CurrentUser user: User, @PathVariable trackingCode: String): ResponseEntity<Any> {
        doormanService.confirmDelivery(user.condominium!!, trackingCode)
        return ResponseEntity.ok().success("Encomenda confirmada com sucesso!")
    }
}