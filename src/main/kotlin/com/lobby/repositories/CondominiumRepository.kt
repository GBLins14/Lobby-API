package com.lobby.repositories

import com.lobby.models.Condominium
import org.springframework.data.jpa.repository.JpaRepository

interface CondominiumRepository : JpaRepository<Condominium, Long> {
    fun findByCnpj(cnpj: String): Condominium?
    fun findByBusinessEmail(businessEmail: String): Condominium?
    fun findByBusinessPhone(businessPhone: String): Condominium?
    fun findByOwnerId(ownerId: Long): Condominium?
    fun findByCondominiumCode(condominiumCode: String): Condominium?
}