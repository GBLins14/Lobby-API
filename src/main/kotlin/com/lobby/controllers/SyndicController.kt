package com.lobby.controllers

import com.lobby.annotations.CurrentUser
import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.extensions.error
import com.lobby.models.User
import com.lobby.services.SyndicService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/syndic")
class SyndicController(
    private val syndicService: SyndicService,
) {
    @GetMapping("/deliveries")
    fun getAllDeliveries(@CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.getAllDeliveries(user.condominium!!)
    }

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
        return syndicService.approveAccount(user.condominium!!, accountId)
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
        return syndicService.updateRole(user.condominium!!, request)
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.banAccount(user.condominium!!, request)
    }

    @PatchMapping("/accounts/unban/{accountId}")
    fun unbanAccount(@PathVariable accountId: Long, @CurrentUser user: User): ResponseEntity<Any> {
        if (user.condominium == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não está registrado em nenhum condomínio.")
        }
        return syndicService.unbanAccount(user.condominium!!, accountId)
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
        return syndicService.deleteAccount(user.condominium!!, accountId)
    }
}