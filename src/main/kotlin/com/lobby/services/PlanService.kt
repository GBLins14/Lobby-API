package com.lobby.services

import com.lobby.enums.SubscriptionPlan
import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import com.stripe.model.checkout.Session
import com.lobby.repositories.CondominiumRepository
import com.stripe.model.Subscription
import com.stripe.param.checkout.SessionCreateParams
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class PlanService(
    private val accountRepository: AccountRepository,
    private val condominiumRepository: CondominiumRepository,
    @Value("\${stripe.plans.basic}") private val STRIPE_PLAN_BASIC: String,
    @Value("\${stripe.plans.professional}") private val STRIPE_PLAN_PROFESSIONAL: String,
    @Value("\${stripe.plans.premium}") private val STRIPE_PLAN_PREMIUM: String,
    @Value("\${app.frontend-url}") private val FRONTEND_URL: String,
) {
    fun getAllPlans(): ResponseEntity<Any> {
        val allPlans = SubscriptionPlan.entries.map { it.name }
        return ResponseEntity.ok(mapOf("success" to true, "plans" to allPlans))
    }

    fun createSubscriptionSession(subscriptionPlan: SubscriptionPlan, userId: Long): String {
        val priceId = when(subscriptionPlan) {
            SubscriptionPlan.BASIC -> STRIPE_PLAN_BASIC
            SubscriptionPlan.PROFESSIONAL -> STRIPE_PLAN_PROFESSIONAL
            SubscriptionPlan.PREMIUM -> STRIPE_PLAN_PREMIUM
            else -> throw IllegalArgumentException("Plano não encontrado")
        }

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)

            .setSuccessUrl("$FRONTEND_URL/payment/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("$FRONTEND_URL/payment/canceled")

            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPrice(priceId)
                    .build()
            )
            .putMetadata("userId", userId.toString())
            .putMetadata("subscriptionPlan", subscriptionPlan.toString())
            .build()

        val session = Session.create(params)

        return session.url
    }

    @Transactional
    fun planCancel(user: User) {
        val subId = user.stripeSubscriptionId

        if (user.subscriptionPlan == null || subId == null) {
            throw IllegalArgumentException("Você não tem nenhum plano ativo para cancelar.")
        }

        try {
            val subscription = Subscription.retrieve(subId)
            subscription.cancel()
        } catch (e: Exception) {
            throw RuntimeException("Erro ao cancelar na Stripe: ${e.message}")
        }
    }
}