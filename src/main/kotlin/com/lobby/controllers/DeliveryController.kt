package com.lobby.controllers

import com.lobby.dto.toResponse
import com.lobby.repositories.DeliveryRepository
import com.lobby.repositories.AccountRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/deliveries")
class DeliveryController(
    private val accountRepository: AccountRepository,
    private val deliveryRepository: DeliveryRepository
) {
    @GetMapping
    fun listMyDeliveries(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<Any> {
        val user = accountRepository.findByUsername(userDetails.username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val deliveries = deliveryRepository.findByResidentId(user.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Nenhuma encomenda encontrada."))

        val response = deliveries.map { it.toResponse() }

        return ResponseEntity.ok(mapOf("success" to true, "deliveries" to response))
    }
}