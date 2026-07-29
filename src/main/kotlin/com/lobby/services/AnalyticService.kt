package com.lobby.services

import com.lobby.exceptions.ConflictException
import com.lobby.models.Analytic
import com.lobby.repositories.AnalyticRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AnalyticService(
    private val analyticRepository: AnalyticRepository
) {
    @Transactional
    fun register(source: String, page: String, userIp: String): String {
        /*val countAnalyticByUserIpAndPage = analyticRepository.findByUserIpAndPage(userIp, page).size

        if (countAnalyticByUserIpAndPage >= 1) {
            throw ConflictException("Já existe um registro de analytics igual para este usuário.")
        }*/

        val analytic = Analytic(
            source = source,
            page = page,
            userIp = userIp
        )
        analyticRepository.save(analytic)

        return "Analytic registrada."
    }

    @Transactional
    fun health(): String {
        val analytic = analyticRepository.findByEvent("Health")
            ?: Analytic(event = "Health")
    
        analytic.lastPing = Instant.now()
    
        analyticRepository.save(analytic)
    
        return "OK"
    }
}
