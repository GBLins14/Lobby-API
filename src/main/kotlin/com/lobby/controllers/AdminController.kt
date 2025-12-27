package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.extensions.error
import com.lobby.models.User
import com.lobby.services.AdminService
import com.lobby.services.SyndicService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService,
    private val syndicService: SyndicService
) {
    @GetMapping("/accounts")
    fun getAccounts(@CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.getAllAccounts(user.condominium!!)
    }

    @GetMapping("/accounts/pendant")
    fun getPendantAccounts(@CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.getPendingAccounts(user.condominium!!)
    }

    @PatchMapping("/accounts/approve/{accountId}")
    fun approveAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return adminService.approveAccount(user.condominium!!, accountId)
    }

    @GetMapping("/accounts/{login}")
    fun getAccount(@PathVariable login: String, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.getAccountByLogin(user.condominium!!, login)
    }

    @GetMapping("/roles")
    fun getRoles(): ResponseEntity<Any> {
        return syndicService.getAllRoles()
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: SetRoleDto, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return adminService.updateRole(user.condominium!!, request)
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return adminService.banAccount(user.condominium!!, request)
    }

    @PatchMapping("/accounts/unban/{accountId}")
    fun unbanAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return adminService.unbanAccount(user.condominium!!, accountId)
    }

    @GetMapping("/accounts/bans")
    fun getBans(@CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.getBannedAccounts(user.condominium!!)
    }

    @DeleteMapping("/accounts/{accountId}")
    fun delAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return adminService.deleteAccount(user.condominium!!, accountId)
    }
}