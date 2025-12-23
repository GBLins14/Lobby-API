package com.lobby.models

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(val user: User) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

    fun getId() = user.id
    override fun getUsername() = user.username
    override fun getPassword() = user.hashedPassword
    fun getApartment() = user.apartmentNumber
    fun getCondominium() = user.condominium
}
