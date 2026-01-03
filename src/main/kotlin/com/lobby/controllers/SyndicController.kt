package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.BanDto
import com.lobby.dto.DeliveryListDto
import com.lobby.dto.SetRoleDto
import com.lobby.dto.UserResponse
import com.lobby.extensions.success
import com.lobby.models.User
import com.lobby.services.SyndicService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/syndic")
class SyndicController(
    private val syndicService: SyndicService,
) {
    @GetMapping("/deliveries")
    fun getAllDeliveries(@CurrentUser user: User): List<DeliveryListDto> {
        val deliveries = syndicService.getAllDeliveries(user.condominium!!)
        return deliveries
    }

    @GetMapping("/accounts")
    fun getAccounts(@CurrentUser user: User) = syndicService.getAllAccounts(user.condominium!!)

    @GetMapping("/accounts/pendant")
    fun getPendantAccounts(@CurrentUser user: User): List<UserResponse> {
        val accounts = syndicService.getPendingAccounts(user.condominium!!)
        return accounts
    }

    @PatchMapping("/accounts/approve/{accountId}")
    fun approveAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        syndicService.approveAccount(user.condominium!!, accountId, user)
        return ResponseEntity.ok().success("Conta aprovada com sucesso! O usuário já pode fazer login.")
    }

    @GetMapping("/accounts/{login}")
    fun getAccount(@PathVariable login: String, @CurrentUser user: User): UserResponse {
        val account = syndicService.getAccountByLogin(user.condominium!!, login)
        return account
    }

    @GetMapping("/roles")
    fun getRoles(@CurrentUser user: User): List<String> {
        val roles = syndicService.getAllRoles(user)
        return roles
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: SetRoleDto, @CurrentUser user: User): ResponseEntity<Any> {
        syndicService.updateRole(user.condominium!!, request, user)
        return ResponseEntity.ok().success("Cargo atualizado com sucesso!")
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto, @CurrentUser user: User): ResponseEntity<Any> {
        syndicService.banAccount(user.condominium!!, request, user)
        return ResponseEntity.ok().success("Conta bloqueada com sucesso.")
    }

    @PatchMapping("/accounts/unban/{accountId}")
    fun unbanAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        syndicService.unbanAccount(user.condominium!!, accountId, user)
        return ResponseEntity.ok().success("Conta desbloqueada com sucesso.")
    }

    @GetMapping("/accounts/bans")
    fun getBans(@CurrentUser user: User): List<UserResponse> {
        val accounts = syndicService.getBannedAccounts(user.condominium!!)
        return accounts
    }

    @DeleteMapping("/accounts/{accountId}")
    fun delAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        syndicService.deleteAccount(user.condominium!!, accountId, user)
        return ResponseEntity.ok().success("Conta deletada com sucesso.")
    }
}