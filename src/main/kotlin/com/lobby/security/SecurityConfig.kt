package com.lobby.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**").permitAll()
                it.requestMatchers("/api/doorman/**").hasRole("DOORMAN")
                it.requestMatchers("/syndic/**").hasRole("SYNDIC")
                it.anyRequest().authenticated()
            }
            .formLogin { it.disable() }

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

