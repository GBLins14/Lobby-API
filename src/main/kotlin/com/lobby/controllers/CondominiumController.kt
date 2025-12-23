package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.SignUpCondominiumDto
import com.lobby.models.User
import com.lobby.services.CondominiumService
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/condominium")
class CondominiumController(
    private val condominiumService: CondominiumService,
) {
    @SecurityRequirements
    @PostMapping("/sign-up")
    fun signUpCondominium(@CurrentUser user: User, @RequestBody request: SignUpCondominiumDto): ResponseEntity<Any> {
        return condominiumService.signUp(user, request)
    }
}
