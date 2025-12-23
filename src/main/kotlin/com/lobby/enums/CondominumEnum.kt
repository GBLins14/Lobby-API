package com.lobby.enums

enum class SubscriptionPlan(val maxBlocks: Int, val maxApartments: Int) {
    TRIAL(Int.MAX_VALUE, Int.MAX_VALUE),
    BASIC(1, 40),
    PROFESSIONAL(3, 100),
    PREMIUM(5, 200),
    BUSINESS(Int.MAX_VALUE, Int.MAX_VALUE)
}