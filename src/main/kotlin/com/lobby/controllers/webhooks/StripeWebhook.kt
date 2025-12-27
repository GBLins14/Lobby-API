package com.lobby.controllers.webhooks

import com.lobby.enums.SubscriptionPlan
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.CondominiumRepository
import com.lobby.services.PlanNotificationService
import com.stripe.model.Invoice
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/webhooks")
class StripeWebhook(
    private val accountRepository: AccountRepository,
    private val condominiumRepository: CondominiumRepository,
    private val planNotificationService: PlanNotificationService,
    @Value("\${stripe.stripe-webhook-secret}") private val endpointSecret: String,
    @Value("\${app.frontend-url}") private val FRONTEND_URL: String
) {

    @PostMapping("/stripe")
    fun handleStripeEvent(
        @RequestBody payload: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val sigHeader = request.getHeader("Stripe-Signature")

        val event = try {
            Webhook.constructEvent(payload, sigHeader, endpointSecret)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature")
        }

        val stripeObject = if (event.dataObjectDeserializer.`object`.isPresent) {
            event.dataObjectDeserializer.`object`.get()
        } else {
            event.dataObjectDeserializer.deserializeUnsafe()
        }

        when (event.type) {
            // 1. Primeira Compra (Checkout)
            "checkout.session.completed" -> {
                (stripeObject as? Session)?.let { handleCheckoutCompleted(it) }
            }

            // 2. Renovação Mensal com Sucesso (Adicionado)
            "invoice.payment_succeeded" -> {
                (stripeObject as? Invoice)?.let { handleRecurringPaymentSuccess(it) }
            }

            // 3. Falha no Pagamento (Cartão recusado)
            "invoice.payment_failed" -> {
                (stripeObject as? Invoice)?.let { handlePaymentFailed(it) }
            }

            // 4. Cancelamento
            "customer.subscription.deleted" -> {
                (stripeObject as? Subscription)?.let { handleSubscriptionDeleted(it) }
            }
        }

        return ResponseEntity.ok("Received")
    }

    private fun handleCheckoutCompleted(session: Session) {
        println("completed")
        val userIdStr = session.metadata["userId"]
        val planName = session.metadata["subscriptionPlan"]

        if (userIdStr != null && planName != null) {
            val userId = userIdStr.toLongOrNull() ?: return
            val account = accountRepository.findById(userId).orElse(null) ?: return

            try {
                account.subscriptionPlan = SubscriptionPlan.valueOf(planName)
                if (session.subscription != null) {
                    account.stripeSubscriptionId = session.subscription
                }
                accountRepository.save(account)

                planNotificationService.sendPaymentSuccess(
                    email = account.email,
                    username = account.username,
                    planName = account.subscriptionPlan?.name ?: "atual",
                    dashboardUrl = "$FRONTEND_URL"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleRecurringPaymentSuccess(invoice: Invoice) {
        val subscriptionId = invoice.subscription
        if (subscriptionId == null) {
            return
        }

        val account = accountRepository.findByStripeSubscriptionId(subscriptionId) ?: return

        planNotificationService.sendPaymentSuccess(
            email = account.email,
            username = account.username,
            planName = account.subscriptionPlan?.name ?: "atual",
            dashboardUrl = "$FRONTEND_URL"
        )
    }

    private fun handlePaymentFailed(invoice: Invoice) {
        val subscriptionId = invoice.subscription
        if (subscriptionId == null) {
            return
        }

        val account = accountRepository.findByStripeSubscriptionId(subscriptionId) ?: return

        planNotificationService.sendPaymentFailure(
            email = account.email,
            username = account.username,
            planName = account.subscriptionPlan?.name ?: "atual",
            billingUrl = "$FRONTEND_URL/subscription"
        )
    }

    @Transactional
    fun handleSubscriptionDeleted(subscription: Subscription) {
        val account = accountRepository.findByStripeSubscriptionId(subscription.id) ?: return

        val condominium = account.condominium

        if (condominium != null) {
            condominiumRepository.delete(condominium)
        } else {
            account.subscriptionPlan = null
            account.stripeSubscriptionId = null
            account.banned = true
            accountRepository.save(account)
        }
    }
}