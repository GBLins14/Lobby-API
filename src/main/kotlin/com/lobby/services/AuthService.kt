package com.lobby.services

import com.lobby.dto.SignInDto
import com.lobby.dto.SignUpDto
import com.lobby.enums.AccountStatus
import com.lobby.dto.toResponseDTO
import com.lobby.enums.Role
import com.lobby.extensions.error
import com.lobby.extensions.success
import com.lobby.models.User
import com.lobby.models.PasswordResetToken
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.CondominiumRepository
import com.lobby.repositories.TokenRepository
import com.lobby.security.Hash
import com.lobby.security.JwtUtil
import com.lobby.utils.checkDuplicate
import com.lobby.utils.generateToken
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthService(
    private val accountRepository: AccountRepository,
    private val condominiumRepository: CondominiumRepository,
    private val tokenRepository: TokenRepository,
    private val jwtUtil: JwtUtil,
    private val bcrypt: Hash,
    private val validatorUtil: ValidatorService,
    private val forgotPasswordService: ForgotPasswordService,
    @Value("\${app.frontend-url}") private val FRONTEND_URL: String,
    @Value("\${app.password-recovery.token-expiration-minutes}") private val TOKEN_EXPIRATION_MINUTES: Long,
    @Value("\${app.sign.min-username-length}") private val MIN_USERNAME_LENGTH: Int,
    @Value("\${app.sign.max-username-length}") private val MAX_USERNAME_LENGTH: Int,
    @Value("\${app.sign.min-password-length}") private val MIN_PASSWORD_LENGTH: Int,
    @Value("\${app.sign.max-password-length}") private val MAX_PASSWORD_LENGTH: Int,
    @Value("\${app.sign.max-attempts}") private val MAX_ATTEMPTS: Int,
    @Value("\${app.sign.lockout-minutes}") private val LOCKOUT_MINUTES: Long
) {
    @Transactional
    fun register(request: SignUpDto): ResponseEntity<Any> {
        val cleanedCpf = validatorUtil.cleanCpfOrCnpj(request.cpf)

        if (!validatorUtil.isValidCpf(cleanedCpf)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("É necessário inserir um número de CPF que seja válido.")
        }

        if (request.username.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("O nome de usuário deve conter no mínimo $MIN_USERNAME_LENGTH caracteres, e no máximo $MAX_USERNAME_LENGTH caracteres.")
        }

        if (!validatorUtil.isValidEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("É necessário inserir um endereço de email que seja válido.")
        }

        if (!validatorUtil.isValidPhone(request.phone)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("É necessário inserir um número de telefone que seja válido.")
        }

        if (request.password.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("A senha deve conter no mínimo $MIN_PASSWORD_LENGTH caracteres, e no máximo $MAX_PASSWORD_LENGTH caracteres.")
        }

        val existingCpf = accountRepository.findByCpf(cleanedCpf)
        val existingUsername = accountRepository.findByUsername(request.username)
        val existingEmail = accountRepository.findByEmail(request.email)
        val existingPhone = accountRepository.findByPhone(request.phone)

        checkDuplicate(existingCpf, "Já existe uma conta registrada com este número de CPF.")?.let { return it }
        checkDuplicate(existingUsername, "Já existe uma conta registrada com este nome de usuário.")?.let { return it }
        checkDuplicate(existingEmail, "Já existe uma conta registrada com este endereço de email.")?.let { return it }
        checkDuplicate(existingPhone, "Já existe uma conta registrada com este número de telefone.")?.let { return it }

        val condominium = if (!request.condominiumCode.isNullOrBlank() && request.role != Role.BUSINESS) {
            condominiumRepository.findByCondominiumCode(request.condominiumCode)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Nenhum condomínio foi encontrado com este código.")
        } else {
            null
        }

        if (condominium == null && request.role != Role.BUSINESS) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("Para se cadastrar como morador, porteiro ou síndico, é obrigatório informar o código do condomínio.")
        }

        if (condominium != null && request.role == Role.BUSINESS) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode criar uma conta empresarial estando registrado em um condomínio existente.")
        }

        val (accountStatus, finalRole, messageReturn) = when (request.role) {
            Role.BUSINESS -> Triple(
                AccountStatus.CREATING,
                Role.BUSINESS,
                "Conta registrada com sucesso."
            )
            Role.SYNDIC -> Triple(
                AccountStatus.PENDING,
                Role.SYNDIC,
                "Conta registrada com sucesso, aguarde a liberação da empresa."
            )
            Role.DOORMAN -> Triple(
                AccountStatus.PENDING,
                Role.DOORMAN,
                "Conta registrada com sucesso, aguarde a liberação de um síndico."
            )
            Role.RESIDENT -> Triple(
                AccountStatus.PENDING,
                Role.RESIDENT,
                "Conta registrada com sucesso, aguarde a liberação de um síndico."
            )
        }

        val user = User(
            cpf = cleanedCpf,
            fullName = request.fullName,
            username = request.username,
            email = request.email,
            phone = request.phone,
            condominium = condominium,
            block = if (finalRole == Role.BUSINESS || finalRole == Role.DOORMAN) null else request.block,
            apartmentNumber = if (finalRole == Role.BUSINESS || finalRole == Role.DOORMAN) null else request.apartmentNumber,
            hashedPassword = bcrypt.encodePassword(request.password),
            role = finalRole,
            accountStatus = accountStatus
        )

        accountRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).success(messageReturn)
    }

    @Transactional
    fun login(request: SignInDto): ResponseEntity<Any> {
        val user = accountRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Usuário ou senha incorretos.")

        if (user.banned) {
            if (user.banExpiresAt == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Sua conta está permanentemente bloqueada.")
            }

            if (!user.isBanExpired()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Conta temporariamente bloqueada. Tente mais tarde.")
            }

            user.apply {
                banned = false
                bannedAt = null
                banExpiresAt = null
                failedLoginAttempts = 0
            }
            accountRepository.save(user)
        }

        val now = Instant.now()

        if (!bcrypt.checkPassword(request.password, user.hashedPassword)) {
            user.failedLoginAttempts += 1

            if (user.failedLoginAttempts >= MAX_ATTEMPTS) {
                user.banned = true
                user.bannedAt = now
                user.banExpiresAt = now.plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES)
                accountRepository.save(user)

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Conta bloqueada devido a tentativas excessivas.")
            }

            accountRepository.save(user)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Usuário ou senha incorretos.")
        }

        if (user.accountStatus == AccountStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("A sua conta ainda não foi aprovada, aguarde a liberação.")
        }

        user.failedLoginAttempts = 0
        accountRepository.save(user)

        val token = jwtUtil.generateToken(user.username, user.role, user.tokenVersion)

        return ResponseEntity.ok(mapOf("success" to true, "token" to token))
    }

    val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun processForgotPassword(email: String) {
        val user = accountRepository.findByEmail(email) ?: return

        tokenRepository.deleteByUser(user)
        tokenRepository.flush()

        val rawToken = generateToken()

        val tokenEntity = PasswordResetToken(
            token = rawToken,
            user = user,
            expiryDate = Instant.now().plus(TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES)
        )
        tokenRepository.save(tokenEntity)

        val link = "$FRONTEND_URL/reset-password?token=$rawToken"

        try {
            forgotPasswordService.send(email, user.username, link)
        } catch (e: Exception) {
            logger.error("Falha ao enviar email de recuperação para $email", e)
        }
    }

    @Transactional
    fun processResetPassword(token: String, newPassword: String): ResponseEntity<Any> {
        val resetToken = tokenRepository.findByToken(token)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Token inválido ou não encontrado.")

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Este link expirou. Solicite uma nova recuperação.")
        }

        if (newPassword.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("A senha deve conter no mínimo $MIN_PASSWORD_LENGTH caracteres, e no máximo $MAX_PASSWORD_LENGTH caracteres.")
        }

        val user = resetToken.user

        user.hashedPassword = bcrypt.encodePassword(newPassword)

        accountRepository.save(user)
        tokenRepository.delete(resetToken)

        return ResponseEntity.status(HttpStatus.OK).success("Sua senha foi alterada com sucesso! Você já pode fazer login.")
    }

    fun getMe(user: User): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf("success" to true, "user" to user.toResponseDTO()))
    }

    @Transactional
    fun logout(user: User): ResponseEntity<Any> {
        user.tokenVersion += 1
        accountRepository.save(user)
        SecurityContextHolder.clearContext()

        return ResponseEntity.status(HttpStatus.OK).success("Logout realizado com sucesso.")
    }
}