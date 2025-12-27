package com.lobby.configs

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class StripeConfig(
    @Value("\${stripe.stripe-api-key}") private val apiKey: String
) {
    @PostConstruct
    fun init() {
        Stripe.apiKey = apiKey
    }
}