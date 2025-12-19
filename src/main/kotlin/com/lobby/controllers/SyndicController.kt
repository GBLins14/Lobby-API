package com.lobby.controllers

import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.services.SyndicService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/syndic")
class SyndicController(
    private val syndicService: SyndicService,
) {
    @GetMapping("/deliveries")
    fun getAllDeliveries(): ResponseEntity<Any> {
        return syndicService.getAllDeliveries()
    }

    @GetMapping("/accounts")
    fun getAccounts(): ResponseEntity<Any> {
        return syndicService.getAllAccounts()
    }

    @GetMapping("/accounts/pendant")
    fun getPendantAccounts(): ResponseEntity<Any> {
        return syndicService.getPendingAccounts()
    }

    @PatchMapping("/accounts/approve/{accountId}")
    fun approveAccount(@PathVariable accountId: Long): ResponseEntity<Any> {
        return syndicService.approveAccount(accountId)
    }

    @GetMapping("/accounts/{accountId}")
    fun getAccount(@PathVariable accountId: Long): ResponseEntity<Any> {
        return syndicService.getAccountByLogin(accountId)
    }

    @GetMapping("/roles")
    fun getRoles(): ResponseEntity<Any> {
        return syndicService.getAllRoles()
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: SetRoleDto): ResponseEntity<Any> {
        return syndicService.updateRole(request)
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto): ResponseEntity<Any> {
        return syndicService.banAccount(request)
    }

    @PatchMapping("/accounts/unban/{accountId}")
    fun unbanAccount(@PathVariable accountId: Long): ResponseEntity<Any> {
        return syndicService.unbanAccount(accountId)
    }

    @GetMapping("/accounts/bans")
    fun getBans(): ResponseEntity<Any> {
        return syndicService.getBannedAccounts()
    }

    @DeleteMapping("/accounts/{accountId}")
    fun delAccount(@PathVariable accountId: Long): ResponseEntity<Any> {
        return syndicService.deleteAccount(accountId)
    }
}