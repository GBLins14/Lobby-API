package com.lobby.dto

import java.time.temporal.ChronoUnit

data class BanDto(
    val login: String,
    val duration: Long? = null,
    val unit: ChronoUnit? = null
)