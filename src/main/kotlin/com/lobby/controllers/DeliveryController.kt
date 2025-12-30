package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.extensions.error
import com.lobby.models.User
import com.lobby.services.DeliveryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/deliveries")
class DeliveryController(
    private val deliveryService: DeliveryService
) {
    @GetMapping
    fun listMyDeliveries(@CurrentUser user: User): ResponseEntity<Any> {
        if (user.apartmentNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Nenhuma encomenda encontrada.")
        }
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        val deliveries = deliveryService.listMyDeliveries(user.condominium!!, user.apartmentNumber!!)
        return ResponseEntity.ok(deliveries)
    }
}