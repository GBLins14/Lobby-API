package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.ForgotPasswordRequest
import com.lobby.dto.ResetPasswordRequest
import com.lobby.dto.SignInDto
import com.lobby.dto.SignUpDto
import com.lobby.extensions.success
import com.lobby.models.CustomUserDetails
import com.lobby.models.User
import com.lobby.services.AuthService
import com.lobby.services.SyndicService
import io.swagger.v3.oas.annotations.security.SecurityRequirements
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
    private val authService: AuthService
) {
    @PostMapping("/sign-up")
    @SecurityRequirements
    fun signUp(@RequestBody request: SignUpDto): ResponseEntity<Any> {
        return authService.register(request)
    }

    @PostMapping("/sign-in")
    @SecurityRequirements
    fun signIn(@RequestBody request: SignInDto): ResponseEntity<Any> {
        return authService.login(request)
    }

    @PostMapping("/forgot-password")
    @SecurityRequirements
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<Any> {
        authService.processForgotPassword(request.email)

        return ResponseEntity.status(HttpStatus.OK).success("Se o e-mail estiver cadastrado, você receberá um link de recuperação.")
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<Any> {
        return authService.processResetPassword(request.token, request.newPassword)
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