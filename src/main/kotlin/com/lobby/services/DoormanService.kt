package com.lobby.services

import com.lobby.dto.CreateDeliveryDto
import com.lobby.dto.toResponse
import com.lobby.enums.DeliveryStatus
import com.lobby.models.Delivery
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import com.lobby.utils.generateCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DoormanService(
    private val deliveryRepository: DeliveryRepository,
    private val accountRepository: AccountRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun registerDelivery(request: CreateDeliveryDto, doormanUsername: String): ResponseEntity<Any> {
        val doorman = accountRepository.findByUsername(doormanUsername)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val apartmentNumber = request.apartmentNumber?.uppercase()?.replace(Regex("[^A-Z0-9]"), "")

        val residents = apartmentNumber?.let { apt ->
            accountRepository.findByApartmentNumber(apt)
        } ?: emptyList()

        val trackingCode = generateCode()

        val delivery = Delivery(
            trackingCode = trackingCode,
            recipientName = request.recipientName,
            apartmentNumber = apartmentNumber,
            doorman = doorman,
            status = DeliveryStatus.WAITING_PICKUP
        )

        deliveryRepository.save(delivery)

        residents.forEach { resident ->
            try {
                notificationService.sendArrivalNotification(
                    recipientName = request.recipientName,
                    email = resident.email,
                    residentName = resident.fullName ?: "Morador",
                    trackingCode = delivery.trackingCode
                )
            } catch (e: Exception) {
                logger.error("Erro ao notificar ${resident.email}", e)
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("success" to true, "message" to "Entrega registrada!"))
    }

    fun getDeliveryByCode(code: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByTrackingCode(code)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Encomenda não encontrada."))

        return ResponseEntity.ok(mapOf("success" to true, "delivery" to delivery.toResponse()))
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
        delivery.withdrawalDate = Instant.now()

        deliveryRepository.save(delivery)

        return ResponseEntity.ok(mapOf("success" to true, "message" to "Entrega confirmada com sucesso!"))
    }
}