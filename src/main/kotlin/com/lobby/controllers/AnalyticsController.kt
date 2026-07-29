package com.lobby.controllers

import com.lobby.dto.AnalyticDto
import com.lobby.extensions.success
import com.lobby.services.AnalyticService
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/analytics")
class AnalyticsController(
    private val analyticService: AnalyticService
) {
    @PostMapping("/create/join")
    @SecurityRequirements
    fun join(req: HttpServletRequest, @RequestBody request: AnalyticDto): ResponseEntity<Any> {
        val userIp = req.getHeader("X-Forwarded-For") ?: req.remoteAddr
        val messageReturn = analyticService.register(request.source, request.page, userIp)
        return ResponseEntity.status(HttpStatus.OK).success(messageReturn)
    }

    @GetMapping("/health")
    @SecurityRequirements
    fun health(): String {
        return "OK"
    }
}
