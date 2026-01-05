package com.lobby.services

import com.lobby.dto.BanDto
import com.lobby.dto.SetRoleDto
import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.lobby.exceptions.BadRequestException
import com.lobby.exceptions.ConflictException
import com.lobby.exceptions.NotFoundException
import com.lobby.exceptions.UnauthorizedException
import com.lobby.models.Condominium
import com.lobby.models.CustomUserDetails
import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import com.lobby.utils.FindAccountOrThrow
import com.lobby.utils.validateHierarchy
import jakarta.transaction.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AdminService(
    private val accountRepository: AccountRepository,
    private val findAccountOrThrow: FindAccountOrThrow
) {
    @Transactional
    fun approveAccount(condominium: Condominium, accountId: Long, user: User) {
        if (user.condominium == null) {
            throw UnauthorizedException("Você não está registrado em nenhum condomínio.")
        }

        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: throw NotFoundException("Conta não encontrada.")

        if (account.accountStatus == AccountStatus.APPROVED) {
            throw ConflictException("Esta conta já está aprovada.")
        }

        account.accountStatus = AccountStatus.APPROVED
        accountRepository.save(account)
    }

    @Transactional
    fun updateRole(condominium: Condominium, request: SetRoleDto, adminAccount: User) {
        val targetAccount = findAccountOrThrow.findAccount(condominium, request.id)
        validateHierarchy(adminAccount, targetAccount)

        if (request.role == Role.SYNDIC || request.role == Role.BUSINESS) {
            throw UnauthorizedException("Você não pode promover alguém a este nível.")
        }

        if (targetAccount.role == request.role) {
            throw ConflictException("A conta já está com este cargo.")
        }

        targetAccount.role = request.role
        accountRepository.save(targetAccount)
    }

    @Transactional
    fun banAccount(condominium: Condominium, request: BanDto, user: User) {
        if (user.condominium == null) {
            throw UnauthorizedException("Você não está registrado em nenhum condomínio.")
        }

        val account = accountRepository.findByCondominiumAndId(condominium, request.id)
            ?: throw NotFoundException("Conta não encontrada.")

        if (!account.isBanExpired()) {
            throw ConflictException("Esta conta já está bloqueada.")
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
    }

    @Transactional
    fun unbanAccount(condominium: Condominium, accountId: Long, user: User) {
        if (user.condominium == null) {
            throw UnauthorizedException("Você não está registrado em nenhum condomínio.")
        }

        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: throw NotFoundException("Conta não encontrada.")

        if (!account.banned) {
            throw BadRequestException("A conta não está banida.")
        }

        account.banned = false
        account.bannedAt = null
        account.banExpiresAt = null
        accountRepository.save(account)
    }

    @Transactional
    fun deleteAccount(condominium: Condominium, accountId: Long, user: User) {
        if (user.condominium == null) {
            throw UnauthorizedException("Você não está registrado em nenhum condomínio.")
        }

        val account = accountRepository.findByCondominiumAndId(condominium, accountId)
            ?: throw NotFoundException("Conta não encontrada.")

        val auth = SecurityContextHolder.getContext().authentication?.principal
        if (auth is CustomUserDetails && auth.user.id == account.id) {
            throw ConflictException("Você não pode deletar sua própria conta enquanto logado.")
        }

        accountRepository.delete(account)
    }
}