package com.lobby.repositories

import com.lobby.models.Delivery
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryRepository : JpaRepository<Delivery, Long> {
    fun findByApartmentNumber(apartmentNumber: String): List<Delivery>?
    fun findByTrackingCode(trackingCode: String): Delivery?
    fun findByDoormanId(doormanId: Long): List<Delivery>?
}