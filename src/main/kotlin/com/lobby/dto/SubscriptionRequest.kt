package com.lobby.dto

import com.lobby.enums.SubscriptionPlan

data class SubscriptionRequest(
    val subscriptionPlan: SubscriptionPlan
)