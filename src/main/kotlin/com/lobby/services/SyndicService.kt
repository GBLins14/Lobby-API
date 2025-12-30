package com.lobby.services

import com.lobby.dto.BanDto
import com.lobby.dto.DeliveryListDto
import com.lobby.dto.SetRoleDto
import com.lobby.dto.UserResponse
import com.lobby.dto.toListResponse
import com.lobby.dto.toResponseDTO
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
import com.lobby.repositories.DeliveryRepository
import jakarta.transaction.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SyndicService(
    private val accountRepository: AccountRepository,
    private val deliveryRepository: DeliveryRepository
) {

    fun getAllDeliveries(condominium: Condominium): List<DeliveryListDto> =
        deliveryRepository.findByCondominium(condominium).map { it.toListResponse() }

    fun getAllAccounts(condominium: Condominium): List<UserResponse> =
        accountRepository.findAllByCondominium(condominium).map { it.toResponseDTO() }

    fun getPendingAccounts(condominium: Condominium): List<UserResponse> {
        val accounts = accountRepository.findByCondominiumAndAccountStatus(condominium, AccountStatus.PENDING)
            ?: emptyList()

        if (accounts.isEmpty()) throw NotFoundException("Nenhuma conta pendente encontrada.")

        return accounts.map { it.toResponseDTO() }
    }

    fun getAccountByLogin(condominium: Condominium, login: String): UserResponse {
        val account = accountRepository.findByCondominiumAndUsernameOrEmail(condominium, login, login)
            ?: throw NotFoundException("Conta não encontrada.")
        return account.toResponseDTO()
    }

    fun getAllRoles(user: User): List<String> {
        if (user.role == Role.SYNDIC) {
            return Role.entries
                .filter { it != Role.SYNDIC && it != Role.BUSINESS }
                .map { it.name }
        }
        return Role.entries
            .filter { it != Role.BUSINESS }
            .map { it.name }
    }

    @Transactional
    fun approveAccount(condominium: Condominium, accountId: Long) {
        val account = findAccountOrThrow(condominium, accountId)
        validateHierarchy(account)

        if (account.accountStatus == AccountStatus.APPROVED) {
            throw ConflictException("Esta conta já está aprovada.")
        }

        account.accountStatus = AccountStatus.APPROVED
        accountRepository.save(account)
    }

    @Transactional
    fun updateRole(condominium: Condominium, request: SetRoleDto, user: User) {
        val account = findAccountOrThrow(condominium, request.id)
        validateHierarchy(account)

        if (user.role == Role.BUSINESS) {
            if (request.role == Role.BUSINESS) {
                throw UnauthorizedException("Você não pode promover alguém a este nível.")
            }
        } else {
            if (request.role == Role.SYNDIC || request.role == Role.BUSINESS) {
                throw UnauthorizedException("Você não pode promover alguém a este nível.")
            }
        }

        if (account.role == request.role) {
            throw ConflictException("A conta já está com este cargo.")
        }

        account.role = request.role
        accountRepository.save(account)
    }

    @Transactional
    fun banAccount(condominium: Condominium, request: BanDto) {
        val account = findAccountOrThrow(condominium, request.id)
        validateHierarchy(account)

        if (!account.isBanExpired()) {
            throw ConflictException("Esta conta já está bloqueada.")
        }

        val now = Instant.now()
        account.apply {
            banned = true
            bannedAt = if (request.duration == null) null else now
            banExpiresAt = if (request.duration == null) null else now.plus(request.duration, request.unit)
            tokenVersion += 1
        }

        accountRepository.save(account)
    }

    @Transactional
    fun unbanAccount(condominium: Condominium, accountId: Long) {
        val account = findAccountOrThrow(condominium, accountId)
        validateHierarchy(account)

        if (!account.banned) {
            throw BadRequestException("A conta não está banida.")
        }

        account.banned = false
        account.bannedAt = null
        account.banExpiresAt = null
        accountRepository.save(account)
    }

    fun getBannedAccounts(condominium: Condominium): List<UserResponse> {
        val accounts = accountRepository.findByCondominiumAndBanned(condominium, true) ?: emptyList()

        if (accounts.isEmpty()) {
            throw NotFoundException("Nenhuma conta banida encontrada.")
        }

        return accounts.map { it.toResponseDTO() }
    }

    @Transactional
    fun deleteAccount(condominium: Condominium, accountId: Long) {
        val account = findAccountOrThrow(condominium, accountId)
        validateHierarchy(account)

        val auth = SecurityContextHolder.getContext().authentication?.principal
        if (auth is CustomUserDetails && auth.user.id == account.id) {
            throw ConflictException("Você não pode deletar sua própria conta enquanto logado.")
        }

        accountRepository.delete(account)
    }

    private fun findAccountOrThrow(condominium: Condominium, id: Long): User {
        return accountRepository.findByCondominiumAndId(condominium, id)
            ?: throw NotFoundException("Conta não encontrada.")
    }

    private fun validateHierarchy(targetAccount: User) {
        if (targetAccount.role == Role.BUSINESS || targetAccount.role == Role.SYNDIC) {
            throw UnauthorizedException("Você não tem permissão para gerenciar um usuário com cargo igual ou superior ao seu.")
        }
    }
}