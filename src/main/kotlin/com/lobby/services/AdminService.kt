package com.lobby.services

import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.enums.AccountStatus
import com.lobby.extensions.error
import com.lobby.extensions.success
import com.lobby.models.Condominium
import com.lobby.models.CustomUserDetails
import com.lobby.repositories.AccountRepository
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AdminService(
    private val accountRepository: AccountRepository
) {
    @Transactional
    fun approveAccount(condominium: Condominium, accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.accountStatus == AccountStatus.APPROVED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("Esta conta já está aprovada.")
        }

        account.accountStatus = AccountStatus.APPROVED
        accountRepository.save(account)

        return ResponseEntity.status(HttpStatus.OK).error("Conta aprovada com sucesso! O usuário ${account.username} já pode fazer login.")
    }

    @Transactional
    fun updateRole(condominium: Condominium, request: SetRoleDto): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, request.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        if (account.role == request.role) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("A conta já está com este cargo.")
        }

        account.role = request.role
        accountRepository.save(account)

        return ResponseEntity.status(HttpStatus.OK).success("Cargo atualizado com sucesso! Usuario: ${account.username}, Cargo: ${request.role}")
    }

    @Transactional
    fun banAccount(condominium: Condominium, request: BanDto): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, request.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

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

        if (!account.banned) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("A conta não está banida.")
        }

        account.banned = false
        account.bannedAt = null
        account.banExpiresAt = null
        accountRepository.save(account)

        return ResponseEntity.status(HttpStatus.OK).success("Conta desbanida com sucesso. ID: $accountId")
    }

    @Transactional
    fun deleteAccount(condominium: Condominium, accountId: Long): ResponseEntity<Any> {
        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).error("Conta não encontrada.")

        val auth = SecurityContextHolder.getContext().authentication?.principal
        if (auth is CustomUserDetails && auth.user.id == account.id) {
            return ResponseEntity.status(HttpStatus.CONFLICT).error("Você não pode deletar sua própria conta enquanto logado.")
        }

        accountRepository.delete(account)
        return ResponseEntity.status(HttpStatus.OK).success("Conta deletada com sucesso. ID: $accountId")
    }
}