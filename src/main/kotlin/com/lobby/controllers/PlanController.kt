package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.SubscriptionRequest
import com.lobby.extensions.error
import com.lobby.models.User
import com.lobby.services.PlanService
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/plan")
class PlanController(
    private val planService: PlanService
) {
    @SecurityRequirements
    @GetMapping
    fun getAllPlans(): ResponseEntity<Any> {
        return planService.getAllPlans()
    }

    @PostMapping("/checkout")
    fun createCheckout(@RequestBody request: SubscriptionRequest, @CurrentUser user: User): ResponseEntity<Any> {
        val checkoutUrl = planService.createSubscriptionSession(request.subscriptionPlan, user.id)

        return ResponseEntity.ok(mapOf("success" to true, "url" to checkoutUrl))
    }

    @PostMapping("/cancel")
    fun planCancel(@CurrentUser user: User): ResponseEntity<Any> {
        return try {
            planService.planCancel(user)
            ResponseEntity.ok().body(mapOf("message" to "Assinatura cancelada com sucesso. Sentiremos sua falta!"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).error("${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Não foi possível cancelar no momento. Tente novamente."))
        }
    }
}