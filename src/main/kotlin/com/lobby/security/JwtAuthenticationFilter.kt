package com.lobby.security

import com.lobby.enums.AccountStatus
import com.lobby.models.CustomUserDetails
import com.lobby.repositories.AccountRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val accountRepository: AccountRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val token = request.getHeader("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        if (token != null && jwtUtil.validateToken(token)) {

            val username = jwtUtil.getUsername(token)
            val user = accountRepository.findByUsername(username)

            if (user != null) {

                val tokenVersion = jwtUtil.getTokenVersion(token)
                if (tokenVersion != user.tokenVersion) {
                    if (unauthorized(response, "Token inválido.")) return
                }

                if (user.accountStatus == AccountStatus.PENDING) {
                    if (unauthorized(response, "A sua conta ainda não foi aprovada, aguarde a liberação.")) return
                }

                if (user.banned) {
                    if (user.banExpiresAt == null) {
                        if (unauthorized(response, "Conta permanentemente bloqueada.")) return
                        return
                    }

                    if (!user.isBanExpired()) {
                        if (unauthorized(response, "Conta temporariamente bloqueada.")) return
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
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun unauthorized(response: HttpServletResponse, msg: String): Boolean {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "text/plain; charset=UTF-8"
        response.characterEncoding = "UTF-8"
        response.writer.write(msg)
        return true
    }
}
