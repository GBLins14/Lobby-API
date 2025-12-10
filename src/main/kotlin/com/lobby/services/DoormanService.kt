package com.lobby.services

import com.lobby.dto.CreateDeliveryDto
import com.lobby.dto.toResponse
import com.lobby.enums.DeliveryStatus
import com.lobby.models.Delivery
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import com.lobby.utils.generateTrackingCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DoormanService(
    private val deliveryRepository: DeliveryRepository,
    private val accountRepository: AccountRepository
) {
    fun registerDelivery(request: CreateDeliveryDto, doormanUsername: String): ResponseEntity<Any> {
        val doorman = accountRepository.findByUsername(doormanUsername)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val resident = accountRepository.findById(request.residentId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Morador não encontrado"))

        val trackingCode = generateTrackingCode()

        val delivery = Delivery(
            trackingCode = trackingCode,
            resident = resident,
            doorman = doorman,
            status = DeliveryStatus.WAITING_PICKUP
        )

        deliveryRepository.save(delivery)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("success" to true, "message" to "Entrega registrada!"))
    }

    fun getDeliveryByCode(code: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByTrackingCode(code)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Encomenda não encontrada."))

        return ResponseEntity.ok(mapOf("success" to true, "message" to delivery.toResponse()))
    }

    fun confirmDelivery(code: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByTrackingCode(code)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Encomenda não encontrada."))

        if (delivery.status == DeliveryStatus.DELIVERED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "Esta encomenda já foi entregue anteriormente."))
        }

        delivery.status = DeliveryStatus.DELIVERED
        delivery.withdrawalDate = LocalDateTime.now()

        deliveryRepository.save(delivery)

        return ResponseEntity.ok(mapOf("success" to true, "message" to "Entrega confirmada com sucesso!"))
    }
}