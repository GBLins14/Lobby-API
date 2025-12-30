package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.dto.UserResponse
import com.lobby.extensions.success
import com.lobby.models.User
import com.lobby.services.AdminService
import com.lobby.services.SyndicService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin")
class AdminController(
    private val adminService: AdminService,
    private val syndicService: SyndicService
) {
    @GetMapping("/accounts")
    fun getAccounts(@CurrentUser user: User): ResponseEntity<List<UserResponse>> {
        val accounts = syndicService.getAllAccounts(user.condominium!!)
        return ResponseEntity.ok(accounts)
    }

    @GetMapping("/accounts/pendant")
    fun getPendantAccounts(@CurrentUser user: User): ResponseEntity<List<UserResponse>> {
        val accounts = syndicService.getPendingAccounts(user.condominium!!)
        return ResponseEntity.ok(accounts)
    }

    @PatchMapping("/accounts/approve/{accountId}")
    fun approveAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        adminService.approveAccount(user.condominium!!, accountId, user)
        return ResponseEntity.ok().success("Conta aprovada com sucesso! O usuário já pode fazer login.")
    }

    @GetMapping("/accounts/{login}")
    fun getAccount(@PathVariable login: String, @CurrentUser user: User): ResponseEntity<UserResponse> {
        val account = syndicService.getAccountByLogin(user.condominium!!, login)
        return ResponseEntity.ok(account)
    }

    @GetMapping("/roles")
    fun getRoles(@CurrentUser user: User): ResponseEntity<List<String>> {
        val roles = syndicService.getAllRoles(user)
        return ResponseEntity.ok(roles)
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: SetRoleDto, @CurrentUser user: User): ResponseEntity<Any> {
        syndicService.updateRole(user.condominium!!, request, user)
        return ResponseEntity.ok().success("Cargo atualizado com sucesso!")
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto, @CurrentUser user: User): ResponseEntity<Any> {
        adminService.banAccount(user.condominium!!, request, user)
        return ResponseEntity.ok().success("Conta bloqueada com sucesso.")
    }

    @PatchMapping("/accounts/unban/{accountId}")
    fun unbanAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        adminService.unbanAccount(user.condominium!!, accountId, user)
        return ResponseEntity.ok().success("Conta desbloqueada com sucesso.")
    }

    @GetMapping("/accounts/bans")
    fun getBans(@CurrentUser user: User): ResponseEntity<List<UserResponse>> {
        val accounts = syndicService.getBannedAccounts(user.condominium!!)
        return ResponseEntity.ok(accounts)
    }

    @DeleteMapping("/accounts/{accountId}")
    fun delAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        adminService.deleteAccount(user.condominium!!, accountId, user)
        return ResponseEntity.ok().success("Conta deletada com sucesso.")
    }
}