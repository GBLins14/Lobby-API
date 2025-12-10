package com.lobby.controllers

import com.lobby.dto.SignInDto
import com.lobby.dto.SignUpDto
import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.lobby.extensions.toResponseDTO
import com.lobby.models.CustomUserDetails
import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import com.lobby.security.Hash
import com.lobby.security.JwtUtil
import com.lobby.services.ValidatorService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

fun checkDuplicate(value: Any?, message: String): ResponseEntity<Any>? {
    return if (value != null) ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("success" to false, "message" to message)) else null
}

@RestController
@RequestMapping("/auth")
class AuthController(
    @Value("\${config.sign.min-username-length}") private val MIN_USERNAME_LENGTH: Int,
    @Value("\${config.sign.max-username-length}") private val MAX_USERNAME_LENGTH: Int,
    @Value("\${config.sign.min-password-length}") private val MIN_PASSWORD_LENGTH: Int,
    @Value("\${config.sign.max-password-length}") private val MAX_PASSWORD_LENGTH: Int,
    @Value("\${config.sign.max-attempts}") private val MAX_ATTEMPTS: Int,
    @Value("\${config.sign.lockout-minutes}") private val LOCKOUT_MINUTES: Long,
    private val accountRepository: AccountRepository, private val jwtUtil: JwtUtil,
    private val bcrypt: Hash, private val validatorUtil: ValidatorService
) {
    @GetMapping("/me")
    fun me(): ResponseEntity<Map<String, Any>> {
        val principal = SecurityContextHolder.getContext().authentication?.principal

        if (principal !is CustomUserDetails) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido ou expirado."))
        }

        val user = principal.user

        val userDto = user.toResponseDTO()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "user" to userDto
            )
        )
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpDto): Any {
        val cleanedCpf = validatorUtil.cleanCpf(request.cpf)

        if (!validatorUtil.isValidCpf(cleanedCpf)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um número de CPF que seja válido."))
        }

        if (request.username.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "O nome de usuário deve conter no mínimo $MIN_USERNAME_LENGTH caracteres, e no máximo $MAX_USERNAME_LENGTH caracteres."))
        }

        if (!validatorUtil.isValidEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um endereço de email que seja válido."))
        }

        if (!validatorUtil.isValidPhone(request.phone)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um número de telefone que seja válido."))
        }

        if (request.password.length !in MIN_PASSWORD_LENGTH .. MAX_PASSWORD_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "A senha deve conter no mínimo $MIN_PASSWORD_LENGTH caracteres, e no máximo $MAX_PASSWORD_LENGTH caracteres."))
        }

        val existingCpf = accountRepository.findByCpf(cleanedCpf)
        val existingUsername = accountRepository.findByUsername(request.username)
        val existingEmail = accountRepository.findByEmail(request.email)
        val existingPhone = accountRepository.findByPhone(request.phone)

        checkDuplicate(existingCpf, "Já existe uma conta registrada com este número de CPF.")?.let { return it }
        checkDuplicate(existingUsername, "Já existe uma conta registrada com este nome de usuário.")?.let { return it }
        checkDuplicate(existingEmail, "Já existe uma conta registrada com este endereço de email.")?.let { return it }
        checkDuplicate(existingPhone, "Já existe uma conta registrada com este número de telefone.")?.let { return it }

        val accountStatus = when (request.role) {
            Role.DOORMAN -> {
                AccountStatus.PENDING
            }
            Role.RESIDENT -> {
                AccountStatus.APPROVED
            }
            else -> {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(
                        mapOf(
                            "success" to false,
                            "message" to "Para se registrar como síndico, é necessario ter permissão."
                        )
                    )
            }
        }

        val user = User(
            role = request.role,
            cpf = cleanedCpf,
            fullName = request.fullName,
            username = request.username,
            email = request.email,
            phone = request.phone,
            hashedPassword = bcrypt.encodePassword(request.password),
            accountStatus = accountStatus
        )

        accountRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("success" to true, "message" to "Conta registrada com sucesso."))
    }

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: SignInDto): ResponseEntity<Any> {
        val user = accountRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Usuário ou senha incorretos."))

        val now = LocalDateTime.now()

        if (user.accountStatus == AccountStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to "A conta ainda não foi aprovada."
                )
            )
        }

        if (user.banned && user.banExpiresAt?.isAfter(now) == true) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to "Conta temporariamente bloqueada."
                )
            )
        }

        if (user.banned && user.banExpiresAt?.isBefore(now) == true) {
            user.banned = false
            user.bannedAt = null
            user.banExpiresAt = null
            user.failedLoginAttempts = 0
            accountRepository.save(user)
        }

        if (user.banned && user.banExpiresAt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to "Conta permanentemente bloqueada."
                )
            )
        }

        if (!bcrypt.checkPassword(request.password, user.hashedPassword)) {
            user.failedLoginAttempts += 1

            if (user.failedLoginAttempts >= MAX_ATTEMPTS) {
                user.banned = true
                user.bannedAt = now
                user.banExpiresAt = now.plusMinutes(LOCKOUT_MINUTES)
                accountRepository.save(user)

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("success" to false, "message" to "Conta bloqueada devido a tentativas excessivas."))
            }

            accountRepository.save(user)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Usuário ou senha incorretos."))
        }

        user.failedLoginAttempts = 0
        accountRepository.save(user)

        val token = jwtUtil.generateToken(user.username, user.tokenVersion)

        return ResponseEntity.ok(mapOf("success" to true, "token" to token))
    }

    @GetMapping("/logout")
    fun logout(): ResponseEntity<Any> {
        val principal = SecurityContextHolder.getContext().authentication?.principal

        if (principal !is CustomUserDetails) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido ou expirado."))
        }

        val user = principal.user

        user.tokenVersion += 1
        accountRepository.save(user)

        SecurityContextHolder.clearContext()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Logout realizado com sucesso."
            )
        )
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
