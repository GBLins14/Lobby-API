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

    @GetMapping("/accounts")
    fun getAccounts(): ResponseEntity<Any> {
        return syndicService.getAllAccounts()
    }

    @GetMapping("/accounts/pendant")
    fun getPendantAccounts(): ResponseEntity<Any> {
        return syndicService.getPendingAccounts()
    }

    @PatchMapping("/accounts/approve/{login}")
    fun approveAccount(@PathVariable login: String): ResponseEntity<Any> {
        return syndicService.approveAccount(login)
    }

    @GetMapping("/accounts/{login}")
    fun getAccount(@PathVariable login: String): ResponseEntity<Any> {
        return syndicService.getAccountByLogin(login)
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: SetRoleDto): ResponseEntity<Any> {
        return syndicService.updateRole(request)
    }

    @GetMapping("/roles")
    fun getRoles(): ResponseEntity<Any> {
        return syndicService.getAllRoles()
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanDto): ResponseEntity<Any> {
        return syndicService.banAccount(request)
    }

    @PatchMapping("/accounts/unban/{login}")
    fun unbanAccount(@PathVariable login: String): ResponseEntity<Any> {
        return syndicService.unbanAccount(login)
    }

    @GetMapping("/accounts/bans")
    fun getBans(): ResponseEntity<Any> {
        return syndicService.getBannedAccounts()
    }

    @DeleteMapping("/accounts/{login}")
    fun delAccount(@PathVariable login: String): ResponseEntity<Any> {
        return syndicService.deleteAccount(login)
    }
}