package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.SignInDto
import com.lobby.dto.SignUpDto
import com.lobby.models.CustomUserDetails
import com.lobby.models.User
import com.lobby.services.AuthService
import com.lobby.services.SyndicService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpDto): ResponseEntity<Any> {
        return authService.register(request)
    }

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: SignInDto): ResponseEntity<Any> {
        return authService.login(request)
    }

    @PostMapping("/logout")
    fun logout(@CurrentUser user: User): ResponseEntity<Any> {
        return authService.logout(user)
    }

    @GetMapping("/me")
    fun me(@CurrentUser user: User): ResponseEntity<Any> {
        return authService.getMe(user)
    }
}