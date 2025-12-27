package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.CreateDeliveryDto
import com.lobby.extensions.error
import com.lobby.models.User
import com.lobby.services.DoormanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doorman/deliveries")
class DoormanController(
    private val doormanService: DoormanService
) {
    @GetMapping
    fun getAllDeliveries(@CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return doormanService.getAllDeliveries(user.condominium!!)
    }

    @PostMapping
    fun create(@RequestBody request: CreateDeliveryDto, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return doormanService.registerDelivery(request, user.condominium!!, user.username)
    }

    @GetMapping("/{trackingCode}")
    fun listTrackingCode(@CurrentUser user: User, @PathVariable trackingCode: String): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return doormanService.getDeliveryByCode(user.condominium!!, trackingCode)
    }

    @PutMapping("/{trackingCode}/confirm")
    fun confirmReceipt(@CurrentUser user: User, @PathVariable trackingCode: String): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return doormanService.confirmDelivery(user.condominium!!, trackingCode)
    }
}