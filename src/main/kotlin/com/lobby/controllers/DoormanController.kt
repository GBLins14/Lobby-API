package com.lobby.controllers

import com.lobby.enums.DeliveryStatus
import com.lobby.models.Delivery
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import com.lobby.dto.CreateDeliveryDto
import com.lobby.dto.toResponse
import com.lobby.utils.generateTrackingCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/doorman/deliveries")
class DoormanController(
    private val deliveryRepository: DeliveryRepository,
    private val accountRepository: AccountRepository
) {
    @PostMapping
    fun create(
        @RequestBody request: CreateDeliveryDto,
        @AuthenticationPrincipal userDetails: UserDetails // Pega o porteiro logado
    ): ResponseEntity<Any> {

        // 1. Achar o Porteiro (Quem está logado)
        val doorman = accountRepository.findByUsername(userDetails.username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 2. Achar o Morador (Quem vai receber)
        val resident = accountRepository.findById(request.residentId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Morador não encontrado"))

        val trackingCode = generateTrackingCode()

        // 3. Criar a Entrega
        val delivery = Delivery(
            trackingCode = trackingCode,
            resident = resident,
            doorman = doorman,
            status = DeliveryStatus.WAITING_PICKUP
        )

        // 4. Salvar
        deliveryRepository.save(delivery)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("success" to true, "message" to "Entrega registrada!"))
    }

    @GetMapping("/{trackingCode}")
    fun listTrackingCode(@PathVariable trackingCode: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByTrackingCode(trackingCode)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Encomenda não encontrada."))

        return ResponseEntity.ok(mapOf("success" to true, "message" to delivery.toResponse()))
    }

    @PutMapping("/{trackingCode}/confirm")
    fun confirmReceipt(@PathVariable trackingCode: String): ResponseEntity<Any> {
        val delivery = deliveryRepository.findByTrackingCode(trackingCode)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Encomenda não encontrada."))

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