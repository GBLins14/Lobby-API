package com.lobby.dto

import java.time.temporal.ChronoUnit

data class BanDto(
    val id: Long,
    val duration: Long? = null,
    val unit: ChronoUnit? = null
)