package com.lobby

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class LobbyApplication

fun main(args: Array<String>) {
    runApplication<LobbyApplication>(*args)
}
