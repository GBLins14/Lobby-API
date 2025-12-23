package com.lobby.repositories

import com.lobby.models.Condominium
import com.lobby.models.Delivery
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryRepository : JpaRepository<Delivery, Long> {
    fun findByCondominium(condominium: Condominium): List<Delivery>
    fun findByCondominiumAndApartmentNumber(condominium: Condominium, apartmentNumber: String): List<Delivery>
    fun findByCondominiumAndTrackingCode(condominium: Condominium, trackingCode: String): Delivery?
    fun findByCondominiumAndDoormanId(condominium: Condominium, doormanId: Long): List<Delivery>?
}