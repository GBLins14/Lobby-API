package com.lobby.controllers

import com.lobby.dto.SignInDto
import com.lobby.dto.SignUpDto
import com.lobby.models.CustomUserDetails
import com.lobby.services.AuthService
import com.lobby.services.SyndicService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val syndicService: SyndicService
) {

    @GetMapping("/me")
    fun me(): ResponseEntity<Any> {
        return authService.getMe()
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpDto): ResponseEntity<Any> {
        return authService.register(request)
    }

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: SignInDto): ResponseEntity<Any> {
        return authService.login(request)
    }

    @GetMapping("/logout")
    fun logout(): ResponseEntity<Any> {
        return authService.logout()
    }

    @GetMapping("/token")
    fun checkToken(): ResponseEntity<Any> {
        val principal = SecurityContextHolder.getContext().authentication?.principal

        if (principal !is CustomUserDetails) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido ou expirado."))
        }

        val user = principal.user

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "id" to user.id,
                "username" to user.username
            )
        )
    }
}