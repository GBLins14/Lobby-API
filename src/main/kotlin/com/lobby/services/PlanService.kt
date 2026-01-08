package com.lobby.services

import com.lobby.enums.SubscriptionPlan
import com.lobby.exceptions.BadRequestException
import com.lobby.exceptions.NotFoundException
import com.lobby.models.User
import com.lobby.repositories.CondominiumRepository
import com.lobby.utils.CancelLocalPlan
import com.lobby.utils.cancelStripeSubscription
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class PlanService(
    private val condominiumRepository: CondominiumRepository,
    private val cancelLocalPlan: CancelLocalPlan,
    @Value("\${stripe.plans.basic}") private val STRIPE_PLAN_BASIC: String,
    @Value("\${stripe.plans.professional}") private val STRIPE_PLAN_PROFESSIONAL: String,
    @Value("\${stripe.plans.premium}") private val STRIPE_PLAN_PREMIUM: String,
    @Value("\${app.frontend-url}") private val FRONTEND_URL: String
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
            else -> throw NotFoundException("Plano não encontrado")
        }

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)

            .setSuccessUrl("$FRONTEND_URL/payment/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("$FRONTEND_URL/plans")

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

    fun cancelPlan(user: User) {
        val subId = user.stripeSubscriptionId

        if (user.subscriptionPlan == null && subId == null) {
            throw BadRequestException("Você não tem nenhum plano ativo para cancelar.")
        }

        if (subId != null) {
            cancelStripeSubscription(subId)
        }

        cancelLocalPlan.cancel(user)
    }

}