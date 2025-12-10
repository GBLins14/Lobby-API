package com.lobby.services

import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.lobby.models.CustomUserDetails
import com.lobby.repositories.AccountRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SyndicService(
    private val accountRepository: AccountRepository
) {

    fun getAllAccounts(): ResponseEntity<Any> {
        val accounts = accountRepository.findAll()
        return ResponseEntity.ok(mapOf("success" to true, "accounts" to accounts))
    }

    fun getPendingAccounts(): ResponseEntity<Any> {
        val accounts = accountRepository.findByAccountStatus(AccountStatus.PENDING)

        if (accounts.isNullOrEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Nenhuma conta pendente encontrada."))
        }
        return ResponseEntity.ok(mapOf("success" to true, "accounts" to accounts))
    }

    fun getAccountByLogin(login: String): ResponseEntity<Any> {
        val account = accountRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))

        return ResponseEntity.ok(mapOf("success" to true, "account" to account))
    }

    fun updateRole(request: SetRoleDto): ResponseEntity<Any> {
        val account = accountRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (account.role == request.role) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "A conta já está com este cargo."))
        }

        account.role = request.role
        accountRepository.save(account)

        return ResponseEntity.ok(
            mapOf("success" to true, "message" to "Cargo atualizado com sucesso. Login: ${request.login}, Cargo: ${request.role}")
        )
    }

    fun getAllRoles(): ResponseEntity<Any> {
        val allRoles = Role.entries.map { it.name }
        return ResponseEntity.ok(mapOf("success" to true, "roles" to allRoles))
    }

    fun banAccount(request: BanDto): ResponseEntity<Any> {
        val account = accountRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))

        val now = LocalDateTime.now()

        // Lógica: Se duration ou unit forem nulos, é ban permanente
        if (request.duration == null || request.unit == null) {
            account.banned = true
            account.bannedAt = null // Permanente não tem data de início específica de "castigo", é estado
            account.banExpiresAt = null
        } else {
            account.banned = true
            account.bannedAt = now
            account.banExpiresAt = now.plus(request.duration, request.unit)
        }

        // Invalida o token atual incrementando a versão
        account.tokenVersion += 1
        accountRepository.save(account)

        // Se o admin se baniu a si mesmo (cuidado!), desloga ele
        checkSelfBan(account.id)

        val typeMsg = if (request.duration == null) "Permanente" else "${request.duration} ${request.unit}"
        return ResponseEntity.ok(
            mapOf("success" to true, "message" to "Conta banida com sucesso. Login: ${request.login}, Tempo: $typeMsg.")
        )
    }

    fun unbanAccount(login: String): ResponseEntity<Any> {
        val account = accountRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (!account.banned) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "A conta não está banida."))
        }

        account.banned = false
        account.bannedAt = null
        account.banExpiresAt = null
        accountRepository.save(account)

        return ResponseEntity.ok(mapOf("success" to true, "message" to "Conta desbanida com sucesso. Login: $login"))
    }

    fun getBannedAccounts(): ResponseEntity<Any> {
        val accounts = accountRepository.findByBanned(true)
        if (accounts.isNullOrEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Nenhuma conta banida encontrada."))
        }
        return ResponseEntity.ok(mapOf("success" to true, "accountsBanned" to accounts))
    }

    fun deleteAccount(login: String): ResponseEntity<Any> {
        val account = accountRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))

        // Opcional: Impedir que o admin se delete a si mesmo
        val auth = SecurityContextHolder.getContext().authentication?.principal
        if (auth is CustomUserDetails && auth.user.id == account.id) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to "Você não pode deletar sua própria conta enquanto logado."))
        }

        accountRepository.delete(account)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Conta deletada com sucesso. Login: $login"))
    }

    // Função auxiliar privada para verificar se o usuário logado afetou a si mesmo
    private fun checkSelfBan(targetUserId: Long) {
        val auth = SecurityContextHolder.getContext().authentication
        val principal = auth?.principal

        if (principal is CustomUserDetails && principal.user.id == targetUserId) {
            SecurityContextHolder.clearContext()
        }
    }
}