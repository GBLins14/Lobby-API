package com.lobby.services

import com.lobby.dto.SignInDto
import com.lobby.dto.SignUpDto
import com.lobby.dto.UserResponse
import com.lobby.enums.AccountStatus
import com.lobby.dto.toResponseDTO
import com.lobby.enums.Role
import com.lobby.exceptions.BadRequestException
import com.lobby.exceptions.ForbiddenException
import com.lobby.exceptions.NotFoundException
import com.lobby.exceptions.UnauthorizedException
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
    @Value("\${app.sign.min-fullname-length}") private val MIN_FULLNAME_LENGTH: Int,
    @Value("\${app.sign.min-username-length}") private val MIN_USERNAME_LENGTH: Int,
    @Value("\${app.sign.max-username-length}") private val MAX_USERNAME_LENGTH: Int,
    @Value("\${app.sign.min-password-length}") private val MIN_PASSWORD_LENGTH: Int,
    @Value("\${app.sign.max-password-length}") private val MAX_PASSWORD_LENGTH: Int,
    @Value("\${app.sign.max-attempts}") private val MAX_ATTEMPTS: Int,
    @Value("\${app.sign.lockout-minutes}") private val LOCKOUT_MINUTES: Long,
    @Value("\${app.condominium.max-accounts_per_apartment}") private val MAX_ACCOUNTS_PER_APARTMENT: Long,
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: SignUpDto): String {
        val cleanedCpf = validatorUtil.cleanCpfOrCnpj(request.cpf)
        val block = request.block?.uppercase()?.trim()
        val apartmentNumber = request.apartmentNumber?.uppercase()?.replace(Regex("[^A-Z0-9]"), "")
        val username = request.username.lowercase().trim()

        if (request.fullName.length < MIN_FULLNAME_LENGTH) {
            throw BadRequestException("É necessário inserir o seu nome completo.")
        }

        if (!validatorUtil.isValidCpf(cleanedCpf)) {
            throw BadRequestException("É necessário inserir um número de CPF que seja válido.")
        }

        if (username.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH) {
            throw BadRequestException("O nome de usuário deve conter no mínimo $MIN_USERNAME_LENGTH caracteres, e no máximo $MAX_USERNAME_LENGTH caracteres.")
        }

        if (!validatorUtil.isValidEmail(request.email)) {
            throw BadRequestException("É necessário inserir um endereço de email que seja válido.")
        }

        if (!validatorUtil.isValidPhone(request.phone)) {
            throw BadRequestException("É necessário inserir um número de telefone que seja válido.")
        }

        if (request.password.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) {
            throw BadRequestException("A senha deve conter no mínimo $MIN_PASSWORD_LENGTH caracteres, e no máximo $MAX_PASSWORD_LENGTH caracteres.")
        }

        val existingCpf = accountRepository.findByCpf(cleanedCpf)
        val existingUsername = accountRepository.findByUsername(username)
        val existingEmail = accountRepository.findByEmail(request.email)
        val existingPhone = accountRepository.findByPhone(request.phone)

        checkDuplicate(existingCpf, "Já existe uma conta registrada com este número de CPF.")
        checkDuplicate(existingUsername, "Já existe uma conta registrada com este nome de usuário.")
        checkDuplicate(existingEmail, "Já existe uma conta registrada com este endereço de email.")
        checkDuplicate(existingPhone, "Já existe uma conta registrada com este número de telefone.")

        val condominium = if (!request.condominiumCode.isNullOrBlank() && request.role != Role.BUSINESS) {
            condominiumRepository.findByCondominiumCode(request.condominiumCode)
                ?: throw NotFoundException("Nenhum condomínio foi encontrado com este código.")
        } else {
            null
        }

        if (condominium == null && request.role != Role.BUSINESS) {
            throw BadRequestException("Para se cadastrar como morador, porteiro ou síndico, é obrigatório informar o código do condomínio.")
        }

        if (request.role == Role.RESIDENT || request.role == Role.SYNDIC) {
            val apartmentsCount = accountRepository.countTotalUnits(condominium!!)

            if (apartmentsCount >= condominium.subscriptionPlan.maxApartments) {
                throw ForbiddenException("O limite máximo de apartamentos para este condomínio foi atingido.")
            }

            if (block.isNullOrBlank() || apartmentNumber.isNullOrBlank()) {
                throw BadRequestException("Para se cadastrar como morador ou síndico, é obrigatório informar o seu bloco e o seu apartamento.")
            }

            val residentsCount = accountRepository.countByCondominiumAndBlockAndApartmentNumber(condominium, block, apartmentNumber)

            if (residentsCount >= MAX_ACCOUNTS_PER_APARTMENT) {
                throw ForbiddenException("O limite máximo de contas para este apartamento foi atingido.")
            }
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
            username = username,
            email = request.email,
            phone = request.phone,
            condominium = condominium,
            block = if (finalRole == Role.BUSINESS || finalRole == Role.DOORMAN) null else block,
            apartmentNumber = if (finalRole == Role.BUSINESS || finalRole == Role.DOORMAN) null else apartmentNumber,
            hashedPassword = bcrypt.encodePassword(request.password),
            role = finalRole,
            accountStatus = accountStatus
        )

        accountRepository.save(user)
        return messageReturn
    }

    @Transactional
    fun login(request: SignInDto): String {
        val login = request.login.lowercase().trim()
        val user = accountRepository.findByUsernameOrEmail(login, login)
            ?: throw UnauthorizedException("Usuário ou senha incorretos.")

        if (user.banned) {
            if (user.banExpiresAt == null) {
                throw UnauthorizedException("Sua conta está permanentemente bloqueada.")
            }

            if (!user.isBanExpired()) {
                throw UnauthorizedException("Conta temporariamente bloqueada. Tente novamente mais tarde.")
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

                throw UnauthorizedException("Conta bloqueada devido a tentativas excessivas.")
            }

            accountRepository.save(user)
            throw UnauthorizedException("Usuário ou senha incorretos.")
        }

        if (user.accountStatus == AccountStatus.PENDING) {
            throw UnauthorizedException("A sua conta ainda não foi aprovada, aguarde a liberação.")
        }

        user.failedLoginAttempts = 0
        accountRepository.save(user)

        val token = jwtUtil.generateToken(user.username, user.role, user.tokenVersion)

        return token
    }

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
    fun processResetPassword(token: String, newPassword: String) {
        val resetToken = tokenRepository.findByToken(token)
            ?: throw NotFoundException("Token inválido ou não encontrado.")

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken)
            throw UnauthorizedException("Este link expirou. Solicite uma nova recuperação.")
        }

        if (newPassword.length !in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) {
            throw BadRequestException("A senha deve conter no mínimo $MIN_PASSWORD_LENGTH caracteres, e no máximo $MAX_PASSWORD_LENGTH caracteres.")
        }

        val user = resetToken.user

        user.hashedPassword = bcrypt.encodePassword(newPassword)
        user.tokenVersion += 1

        accountRepository.save(user)
        tokenRepository.delete(resetToken)
        SecurityContextHolder.clearContext()
    }

    fun getMe(user: User): UserResponse {
        return user.toResponseDTO()
    }

    @Transactional
    fun logout(user: User) {
        user.tokenVersion += 1
        accountRepository.save(user)
        SecurityContextHolder.clearContext()
    }
}
