package com.lobby.utils

import com.stripe.model.Subscription

fun cancelStripeSubscription(subId: String) {
    try {
        val subscription = Subscription.retrieve(subId)
        subscription.cancel()
    } catch (e: Exception) {
        throw RuntimeException("Erro ao cancelar assinatura na Stripe", e)
    }
}
