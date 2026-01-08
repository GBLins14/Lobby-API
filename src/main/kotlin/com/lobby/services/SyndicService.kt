package com.lobby.services

import com.lobby.dto.BanDto
import com.lobby.dto.DeliveryResponseDto
import com.lobby.dto.SetRoleDto
import com.lobby.dto.UserResponse
import com.lobby.dto.toResponse
import com.lobby.dto.toResponseDTO
import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.lobby.exceptions.BadRequestException
import com.lobby.exceptions.ConflictException
import com.lobby.exceptions.NotFoundException
import com.lobby.exceptions.UnauthorizedException
import com.lobby.models.Condominium
import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.DeliveryRepository
import com.lobby.utils.FindAccountOrThrow
import com.lobby.utils.validateHierarchy
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SyndicService(
    private val accountRepository: AccountRepository,
    private val deliveryRepository: DeliveryRepository,
    private val findAccountOrThrow: FindAccountOrThrow
) {

    fun getAllDeliveries(condominium: Condominium): List<DeliveryResponseDto> =
        deliveryRepository.findByCondominium(condominium).map { it.toResponse() }

    fun getAllAccounts(condominium: Condominium): List<UserResponse> =
        accountRepository.findAllByCondominium(condominium).map { it.toResponseDTO() }

    fun getPendingAccounts(condominium: Condominium): List<UserResponse> {
        val accounts = accountRepository.findByCondominiumAndAccountStatus(condominium, AccountStatus.PENDING)
            ?: emptyList()

        if (accounts.isEmpty()) throw NotFoundException("Nenhuma conta pendente encontrada.")

        return accounts.map { it.toResponseDTO() }
    }

    fun getAccountById(condominium: Condominium, id: Long): UserResponse {
        val account = accountRepository.findByCondominiumAndId(condominium, id)
            ?: throw NotFoundException("Conta não encontrada.")
        return account.toResponseDTO()
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
    fun approveAccount(condominium: Condominium, accountId: Long, syndicAccount: User) {
        val targetAccount = findAccountOrThrow.findAccount(condominium, accountId)
        validateHierarchy(syndicAccount, targetAccount)

        if (targetAccount.accountStatus == AccountStatus.APPROVED) {
            throw ConflictException("Esta conta já está aprovada.")
        }

        targetAccount.accountStatus = AccountStatus.APPROVED
        accountRepository.save(targetAccount)
    }

    @Transactional
    fun updateRole(condominium: Condominium, request: SetRoleDto, syndicAccount: User) {
        val targetAccount = findAccountOrThrow.findAccount(condominium, request.id)
        validateHierarchy(syndicAccount, targetAccount)

        if (request.role == Role.BUSINESS) {
            throw UnauthorizedException("Você não pode promover alguém a este nível.")
        }

        if (targetAccount.role == request.role) {
            throw ConflictException("A conta já está com este cargo.")
        }

        targetAccount.role = request.role
        accountRepository.save(targetAccount)
    }

    @Transactional
    fun banAccount(condominium: Condominium, request: BanDto, syndicAccount: User) {
        val targetAccount = findAccountOrThrow.findAccount(condominium, request.id)
        validateHierarchy(syndicAccount, targetAccount)

        if (targetAccount.banned) {
            throw ConflictException("Esta conta já está bloqueada.")
        }

        val now = Instant.now()
        targetAccount.apply {
            banned = true
            bannedAt = if (request.duration == null) null else now
            banExpiresAt = if (request.duration == null) null else now.plus(request.duration, request.unit)
            tokenVersion += 1
        }

        accountRepository.save(targetAccount)
    }

    @Transactional
    fun unbanAccount(condominium: Condominium, accountId: Long, syndicAccount: User) {
        val targetAccount = findAccountOrThrow.findAccount(condominium, accountId)
        validateHierarchy(syndicAccount, targetAccount)

        if (!targetAccount.banned) {
            throw BadRequestException("A conta não está banida.")
        }

        targetAccount.banned = false
        targetAccount.bannedAt = null
        targetAccount.banExpiresAt = null
        accountRepository.save(targetAccount)
    }

    fun getBannedAccounts(condominium: Condominium): List<UserResponse> {
        val accounts = accountRepository.findByCondominiumAndBanned(condominium, true) ?: emptyList()

        if (accounts.isEmpty()) {
            throw NotFoundException("Nenhuma conta banida encontrada.")
        }

        return accounts.map { it.toResponseDTO() }
    }

    @Transactional
    fun deleteAccount(condominium: Condominium, accountId: Long, syndicAccount: User) {
        val targetAccount = findAccountOrThrow.findAccount(condominium, accountId)
        validateHierarchy(syndicAccount, targetAccount)

        accountRepository.delete(targetAccount)
    }
}