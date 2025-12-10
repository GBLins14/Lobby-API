package com.lobby.controllers

import com.lobby.dto.AccountsListDto
import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.enums.AccountStatus
import com.lobby.models.CustomUserDetails
import com.lobby.enums.Role
import com.lobby.repositories.AccountRepository
import com.lobby.services.RequireAdminService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/admin")
class AdminAccountController(private val accountRepository: AccountRepository, private val requireAdminService: RequireAdminService) {

    @GetMapping("/accounts")
    fun getAccounts(): ResponseEntity<Any> {
        val accounts = accountRepository.findAll()
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "accounts" to accounts
        ))
    }

    @GetMapping("/accounts/pendant")
    fun getPendantAccounts(): ResponseEntity<Any> {
        val accounts = accountRepository.findByAccountStatus(AccountStatus.PENDING)

        if (accounts.isNullOrEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Nenhuma conta pendente encontrada."))
        }

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "accounts" to accounts
            )
        )
    }

    @GetMapping("/account")
    fun getAccount(@RequestBody request: AccountsListDto): ResponseEntity<Any> {
        val account = accountRepository.findByUsernameOrEmail(request.login, request.login)
        return if (account != null) {
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "account" to account
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))
        }
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: SetRoleDto): ResponseEntity<Any> {
        val accountReq = accountRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (accountReq.role == request.role) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("success" to false, "message" to "A conta já está com este cargo."))
        }

        accountReq.role = request.role
        accountRepository.save(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Cargo atualizado com sucesso. Login: ${request.login}, Cargo: ${request.role}"
            )
        )
    }

    @GetMapping("/roles")
    fun getRoles(): ResponseEntity<Any> {
        val allRoles = Role.entries.map { it.name }
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "roles" to allRoles
        ))
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto): ResponseEntity<Any> {
        val accountReq = accountRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))


        if (request.duration == null || request.unit == null) {
            accountReq.banned = true
            accountReq.bannedAt = null
            accountReq.banExpiresAt = null

            accountReq.tokenVersion += 1
            accountRepository.save(accountReq)

            val auth = SecurityContextHolder.getContext().authentication
            val principal = auth?.principal

            if (principal is CustomUserDetails && principal.user.id == accountReq.id) {
                SecurityContextHolder.clearContext()
            }

            return ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Conta banida com sucesso. Login: ${request.login}, Tempo: Permanente."
                )
            )
        }

        val now = LocalDateTime.now()
        val banExpiresAt = now.plus(request.duration, request.unit)

        accountReq.banned = true
        accountReq.bannedAt = now
        accountReq.banExpiresAt = banExpiresAt

        accountReq.tokenVersion += 1
        accountRepository.save(accountReq)

        val auth = SecurityContextHolder.getContext().authentication
        val principal = auth?.principal

        if (principal is CustomUserDetails && principal.user.id == accountReq.id) {
            SecurityContextHolder.clearContext()
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Conta banida com sucesso. Login: ${request.login}, Tempo: ${request.duration} ${request.unit}."
            )
        )
    }

    @PatchMapping("/accounts/unban/{login}")
    fun unbanAccount(@PathVariable login: String): ResponseEntity<Any> {
        val accountReq = accountRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (!accountReq.banned) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("success" to false, "message" to "A conta não está banida."))
        }

        accountReq.banned = false
        accountReq.bannedAt = null
        accountReq.banExpiresAt = null
        accountRepository.save(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Conta desbanida com sucesso. Login: $login"
            )
        )
    }

    @GetMapping("/accounts/bans")
    fun getBans(): ResponseEntity<Any> {
        val accountsBanned = accountRepository.findByBanned(true)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Nenhuma conta banida encontrada."))
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "accountsBanned" to accountsBanned
        ))
    }

    @DeleteMapping("/accounts/{login}")
    fun delAccount(@PathVariable login: String): ResponseEntity<Any> {
        val accountReq = accountRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        accountRepository.delete(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Conta deletada com sucesso. Login: $login"
            )
        )
    }
}
