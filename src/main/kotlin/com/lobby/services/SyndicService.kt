package com.lobby.services

import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.dto.toListResponse
import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.lobby.extensions.error
import com.lobby.extensions.success
import com.lobby.models.Condominium
import com.lobby.models.CustomUserDetails
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SyndicService(
    private val accountRepository: AccountRepository,
    private val deliveryRepository: DeliveryRepository
) {
    fun getAllDeliveries(condominium: Condominium): ResponseEntity<Any> {
        val deliveries = deliveryRepository.findByCondominium(condominium).map { it.toListResponse() }

        return ResponseEntity.status(HttpStatus.OK)
            .body(mapOf("success" to true, "deliveries" to deliveries))
    }

    fun getAllAccounts(condominium: Condominium): ResponseEntity<Any> {
        val accounts = accountRepository.findAllByCondominium(condominium)
        return ResponseEntity.ok(mapOf("success" to true, "accounts" to accounts))
    }

    fun getPendingAccounts(condominium: Condominium): ResponseEntity<Any> {
        val accounts = accountRepository.findByCondominiumAndAccountStatus(condominium, AccountStatus.PENDING)

        if (accounts.isNullOrEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Nenhuma conta pendente encontrada.")
        }
        return ResponseEntity.ok(mapOf("success" to true, "accounts" to accounts))
    }

    @Transactional
    fun approveAccount(condominium: Condominium, accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.role == Role.BUSINESS || account.role == Role.SYNDIC) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode aprovar uma conta com o cargo maior ou igual o seu, solicite a aprovação para a empresa!")
        }

        if (account.accountStatus == AccountStatus.APPROVED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("Esta conta já está aprovada.")
        }

        account.accountStatus = AccountStatus.APPROVED
        accountRepository.save(account)

        return ResponseEntity.status(HttpStatus.OK).error("Conta aprovada com sucesso! O usuário ${account.username} já pode fazer login.")
    }

    fun getAccountByLogin(condominium: Condominium, login: String): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndUsernameOrEmail(condominium, login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        return ResponseEntity.ok(mapOf("success" to true, "accounts" to account))
    }

    @Transactional
    fun updateRole(condominium: Condominium, request: SetRoleDto): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, request.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.role == Role.BUSINESS || account.role == Role.SYNDIC) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode editar uma conta com o cargo maior ou igual o seu, solicite a mudança para a empresa!")
        }

        if (account.role == request.role) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("A conta já está com este cargo.")
        }

        account.role = request.role
        accountRepository.save(account)

        return ResponseEntity.status(HttpStatus.OK).success("Cargo atualizado com sucesso. ID: ${request.id}, Cargo: ${request.role}")
    }

    fun getAllRoles(): ResponseEntity<Any> {
        val allRoles = Role.entries.map { it.name }
        return ResponseEntity.ok(mapOf("success" to true, "roles" to allRoles))
    }

    @Transactional
    fun banAccount(condominium: Condominium, request: BanDto): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, request.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.role == Role.BUSINESS || account.role == Role.SYNDIC) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode bloquear uma conta com o cargo maior ou igual o seu, solicite o bloqueamento para a empresa!")
        }

        if (!account.isBanExpired()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("Esta conta já está bloqueada.")
        }

        val now = Instant.now()

        if (request.duration == null || request.unit == null) {
            account.banned = true
            account.bannedAt = null
            account.banExpiresAt = null
        } else {
            account.banned = true
            account.bannedAt = now
            account.banExpiresAt = now.plus(request.duration, request.unit)
        }

        account.tokenVersion += 1
        accountRepository.save(account)

        val typeMsg = if (request.duration == null) "Permanente" else "${request.duration} ${request.unit}"
        return ResponseEntity.status(HttpStatus.OK).success("Conta bloqueada com sucesso. ID: ${request.id}, Tempo: $typeMsg.")
    }

    @Transactional
    fun unbanAccount(condominium: Condominium, accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.role == Role.BUSINESS || account.role == Role.SYNDIC) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode desbloquear uma conta com o cargo maior ou igual o seu, solicite o desbloqueamento para a empresa!")
        }

        if (!account.banned) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("A conta não está banida.")
        }

        account.banned = false
        account.bannedAt = null
        account.banExpiresAt = null
        accountRepository.save(account)

        return ResponseEntity.status(HttpStatus.OK).success("Conta desbanida com sucesso. ID: $accountId")
    }

    fun getBannedAccounts(condominium: Condominium): ResponseEntity<Any> {
        val accounts = accountRepository.findByCondominiumAndBanned(condominium, true)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Nenhuma conta banida encontrada.")

        return ResponseEntity.ok(mapOf("success" to true, "accounts" to accounts))
    }

    @Transactional
    fun deleteAccount(condominium: Condominium, accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.role == Role.BUSINESS || account.role == Role.SYNDIC) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode excluir uma conta com o cargo maior ou igual o seu, solicite a exclusão para a empresa!")
        }

        val auth = SecurityContextHolder.getContext().authentication?.principal
        if (auth is CustomUserDetails && auth.user.id == account.id) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("Você não pode deletar sua própria conta enquanto logado.")
        }

        accountRepository.delete(account)
        return ResponseEntity.status(HttpStatus.OK).success("Conta deletada com sucesso. ID: $accountId")
    }
}