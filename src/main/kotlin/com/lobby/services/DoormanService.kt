package com.lobby.services

import com.lobby.dto.CreateDeliveryDto
import com.lobby.dto.toListResponse
import com.lobby.dto.toResponse
import com.lobby.enums.DeliveryStatus
import com.lobby.extensions.error
import com.lobby.extensions.success
import com.lobby.models.Condominium
import com.lobby.models.Delivery
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import com.lobby.utils.generateCode
import jakarta.transaction.Transactional
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

    fun getAllDeliveries(condominium: Condominium): ResponseEntity<Any> {
        val deliveries = deliveryRepository.findByCondominium(condominium).map { it.toListResponse() }

        return ResponseEntity.status(HttpStatus.OK)
            .body(mapOf("success" to true, "deliveries" to deliveries))
    }

    @Transactional
    fun registerDelivery(request: CreateDeliveryDto, condominium: Condominium, doormanUsername: String): ResponseEntity<Any> {
        val doorman = accountRepository.findByUsername(doormanUsername)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Ocorreu um erro com sua conta, tente novamente mais tarde.")

        val apartmentNumber = request.apartmentNumber?.uppercase()?.replace(Regex("[^A-Z0-9]"), "")

        val residents = apartmentNumber?.let { apt ->
            accountRepository.findByCondominiumAndApartmentNumber(condominium, apt)
        } ?: emptyList()

        val trackingCode = generateCode()

        val delivery = Delivery(
            condominium = condominium,
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

        return ResponseEntity.status(HttpStatus.CREATED).success("Entrega registrada!")
    }

    fun getDeliveryByCode(condominium: Condominium, code: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByCondominiumAndTrackingCode(condominium, code)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Encomenda não encontrada.")

        return ResponseEntity.ok(mapOf("success" to true, "delivery" to delivery.toResponse()))
    }

    @Transactional
    fun confirmDelivery(condominium: Condominium, code: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByCondominiumAndTrackingCode(condominium, code)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Encomenda não encontrada.")

        if (delivery.status == DeliveryStatus.DELIVERED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("Esta encomenda já foi entregue anteriormente.")
        }

        delivery.status = DeliveryStatus.DELIVERED
        delivery.withdrawalDate = Instant.now()

        deliveryRepository.save(delivery)

        return ResponseEntity.status(HttpStatus.OK).success("Entrega confirmada com sucesso!")
    }
}