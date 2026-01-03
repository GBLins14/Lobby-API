package com.lobby.services

import com.lobby.dto.DeliveryResponseDto
import com.lobby.dto.toResponse
import com.lobby.exceptions.BadRequestException
import com.lobby.exceptions.NotFoundException
import com.lobby.models.Condominium
import com.lobby.repositories.DeliveryRepository
import org.springframework.stereotype.Service

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository
) {
    fun listMyDeliveries(condominium: Condominium?, block: String?, apartmentNumber: String?): List<DeliveryResponseDto> {
        if (apartmentNumber == null || block == null) {
            throw NotFoundException("Nenhuma encomenda encontrada.")
        }
        if (condominium == null) {
            throw BadRequestException("Você não está registrado em nenhum condomínio.")
        }

        val block = block.uppercase().trim()
        val apartmentNumber = apartmentNumber.uppercase().replace(Regex("[^A-Z0-9]"), "")
        val deliveries = deliveryRepository.findByCondominiumAndBlockAndApartmentNumber(condominium, block, apartmentNumber)
        val response = deliveries.map { it.toResponse() }

        return response
    }
}