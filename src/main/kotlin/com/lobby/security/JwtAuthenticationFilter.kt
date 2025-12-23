package com.lobby.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.lobby.enums.AccountStatus
import com.lobby.models.CustomUserDetails
import com.lobby.repositories.AccountRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val accountRepository: AccountRepository,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        if (token == null || !jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response)
            return
        }

        val username = jwtUtil.getUsername(token)
        val user = accountRepository.findByUsername(username)

        if (user == null) {
            filterChain.doFilter(request, response)
            return
        }

        val tokenVersion = jwtUtil.getTokenVersion(token)
        if (tokenVersion != user.tokenVersion) {
            sendErrorJson(response, "Sessão expirada. Faça login novamente.")
            return
        }

        if (user.accountStatus == AccountStatus.PENDING) {
            sendErrorJson(response, "A sua conta ainda não foi aprovada, aguarde a liberação.")
            return
        }

        if (user.banned) {
            if (user.banExpiresAt == null) {
                sendErrorJson(response, "Sua conta está permanentemente bloqueada.")
                return
            }

            if (!user.isBanExpired()) {
                sendErrorJson(response, "Conta temporariamente bloqueada. Tente mais tarde.")
                return
            }

            user.apply {
                banned = false
                bannedAt = null
                banExpiresAt = null
                failedLoginAttempts = 0
            }
            accountRepository.save(user)
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name.uppercase()}"))
        val authentication = UsernamePasswordAuthenticationToken(
            CustomUserDetails(user),
            null,
            authorities
        )
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }

    private fun sendErrorJson(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        val jsonPayload = mapOf(
            "success" to false,
            "message" to message
        )

        response.writer.write(objectMapper.writeValueAsString(jsonPayload))
    }
}