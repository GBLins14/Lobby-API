package com.lobby.services

import com.lobby.dto.SignUpCondominiumDto
import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.lobby.extensions.error
import com.lobby.models.Condominium
import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.CondominiumRepository
import com.lobby.utils.checkDuplicate
import com.lobby.utils.generateCode
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class CondominiumService(
    private val validatorUtil: ValidatorService,
    private val accountRepository: AccountRepository,
    private val condominiumRepository: CondominiumRepository,
    @Value("\${app.condominium.min-condominium_name_length}") private val MIN_CONDOMINIUM_NAME_LENGTH: Int,
    @Value("\${app.condominium.max-condominium_name_length}") private val MAX_CONDOMINIUM_NAME_LENGTH: Int
) {
    @Transactional
    fun signUp(user: User, request: SignUpCondominiumDto): ResponseEntity<Any> {
        val cleanedCnpj = validatorUtil.cleanCpfOrCnpj(request.cnpj)

        val userPlan = user.subscriptionPlan
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Adquira um plano para poder registrar um condomínio.")

        if (!validatorUtil.isValidCnpj(cleanedCnpj)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("É necessário inserir um número de CNPJ que seja válido.")
        }

        if (request.name.length !in MIN_CONDOMINIUM_NAME_LENGTH..MAX_CONDOMINIUM_NAME_LENGTH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("O nome do seu condomínio deve conter no mínimo $MIN_CONDOMINIUM_NAME_LENGTH caracteres, e no máximo $MAX_CONDOMINIUM_NAME_LENGTH caracteres.")
        }

        if (!validatorUtil.isValidEmail(request.businessEmail)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("É necessário inserir um endereço de email que seja válido.")
        }

        if (!validatorUtil.isValidPhone(request.businessPhone)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).error("É necessário inserir um número de telefone que seja válido.")
        }

        val existingCnpj = condominiumRepository.findByCnpj(cleanedCnpj)
        val existingBusinessEmail = condominiumRepository.findByBusinessEmail(request.businessEmail)
        val existingBusinessPhone = condominiumRepository.findByBusinessPhone(request.businessPhone)
        val existingByOwner = condominiumRepository.findByOwnerId(user.id)

        checkDuplicate(existingCnpj, "Já existe um condomínio registrado com este CNPJ.")?.let { return it }
        checkDuplicate(existingBusinessEmail, "Já existe um condomínio registrado com este endereço de email.")?.let { return it }
        checkDuplicate(existingBusinessPhone, "Já existe um condomínio registrado com este número de telefone.")?.let { return it }
        checkDuplicate(existingByOwner, "Já existe um condomínio registrado em sua conta.")?.let { return it }

        if (user.role != Role.BUSINESS) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode registrar um condomínio fora de uma conta empresarial.")
        }

        if (user.condominium != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("Você não pode registrar um condomínio estando registrado em um condomínio existente.")
        }

        if (request.blocksCount > userPlan.maxBlocks) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("O limite de blocos do seu plano atual é de ${userPlan.maxBlocks}.")
        }

        if (request.apartmentCount > userPlan.maxApartments) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).error("O limite de apartamentos do seu plano atual é de ${userPlan.maxApartments}.")
        }

        val condominiumCode = generateCode()

        val condominium = Condominium(
            name = request.name,
            cnpj = cleanedCnpj,
            businessEmail = request.businessEmail,
            businessPhone = request.businessPhone,
            ownerId = user.id,
            condominiumCode = condominiumCode,
            blocksCount = request.blocksCount,
            apartmentCount = request.apartmentCount,
            address = request.address,
            subscriptionPlan = userPlan
        )

        val savedCondominium = condominiumRepository.save(condominium)

        user.apply {
            this.condominium = savedCondominium
            this.accountStatus = AccountStatus.APPROVED
        }
        accountRepository.save(user)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf(
                "success" to true,
                "message" to "Condomínio registrado com sucesso.",
                "condominiumCode" to condominiumCode
            ))
    }

    @Transactional
    fun deleteCondominium(id: Long) {
        accountRepository.deleteByCondominiumId(id)
        condominiumRepository.deleteById(id)
    }
}