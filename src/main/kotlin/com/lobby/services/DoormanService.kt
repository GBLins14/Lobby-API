package com.lobby.services

import com.lobby.dto.CreateDeliveryDto
import com.lobby.dto.DeliveryListDto
import com.lobby.dto.DeliveryResponseDto
import com.lobby.dto.toListResponse
import com.lobby.dto.toResponse
import com.lobby.enums.DeliveryStatus
import com.lobby.exceptions.ConflictException
import com.lobby.exceptions.NotFoundException
import com.lobby.exceptions.UnauthorizedException
import com.lobby.models.Condominium
import com.lobby.models.Delivery
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import com.lobby.utils.generateCode
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DoormanService(
    private val deliveryRepository: DeliveryRepository,
    private val accountRepository: AccountRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun getAllDeliveries(condominium: Condominium): List<DeliveryListDto> {
        val deliveries = deliveryRepository.findByCondominium(condominium).map { it.toListResponse() }

        return deliveries
    }

    @Transactional
    fun registerDelivery(request: CreateDeliveryDto, condominium: Condominium, doormanUsername: String) {
        val doorman = accountRepository.findByUsername(doormanUsername)
            ?: throw UnauthorizedException("Ocorreu um erro com sua conta, tente novamente mais tarde.")

        val block = request.block.uppercase().trim()
        val apartmentNumber = request.apartmentNumber.uppercase().replace(Regex("[^A-Z0-9]"), "")

        val residents = accountRepository.findByCondominiumAndBlockAndApartmentNumber(condominium, block, apartmentNumber)
            ?: emptyList()

        if (residents.isEmpty()) {
            throw NotFoundException("Nenhum morador encontrado para este bloco/apartamento.")
        }

        val trackingCode = generateCode()

        val delivery = Delivery(
            condominium = condominium,
            trackingCode = trackingCode,
            recipientName = request.recipientName,
            block = block,
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
    }

    fun getDeliveryByCode(condominium: Condominium, code: String): DeliveryResponseDto {
        val delivery = deliveryRepository.findByCondominiumAndTrackingCode(condominium, code)
            ?: throw NotFoundException("Encomenda não encontrada.")

        return delivery.toResponse()
    }

    @Transactional
    fun confirmDelivery(condominium: Condominium, code: String) {
        val trackingCode = code.uppercase().trim()
        val delivery = deliveryRepository.findByCondominiumAndTrackingCode(condominium, trackingCode)
            ?: throw NotFoundException("Encomenda não encontrada.")

        if (delivery.status == DeliveryStatus.DELIVERED) {
            throw ConflictException("Esta encomenda já foi entregue anteriormente.")
        }

        delivery.status = DeliveryStatus.DELIVERED
        delivery.withdrawalDate = Instant.now()

        deliveryRepository.save(delivery)
    }
}