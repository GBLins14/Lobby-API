package com.lobby.services

import com.lobby.dto.toResponse
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository,
    private val accountRepository: AccountRepository
) {
    fun listMyDeliveries(username: String): ResponseEntity<Any> {
        val user = accountRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val deliveries = deliveryRepository.findByResidentId(user.id)

        if (deliveries.isNullOrEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Nenhuma encomenda encontrada."))
        }

        val response = deliveries.map { it.toResponse() }

        return ResponseEntity.ok(mapOf("success" to true, "deliveries" to response))
    }
}