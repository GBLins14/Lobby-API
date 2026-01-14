package com.lobby.repositories

import org.springframework.data.jpa.repository.JpaRepository
import com.lobby.models.Analytic

interface AnalyticRepository : JpaRepository<Analytic, Long> {
    fun findByUserIpAndPage(userIp: String, source: String): List<Analytic>
}