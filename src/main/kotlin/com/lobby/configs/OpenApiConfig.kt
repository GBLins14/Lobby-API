package com.lobby.configs

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Lobby API")
                    .version("1.0.0")
                    .description("API de gestão de encomendas para condomínios.")
                    .contact(Contact().name("Gabriel Lins").email("gabrielglins18@gmail.com"))
            )
    }
}