package com.lobby.configs

import com.resend.Resend
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResendConfig {
    @Bean
    fun resendClient(@Value("\${spring.mail.password}") apiKey: String): Resend {
        return Resend(apiKey)
    }
}