package com.votaciones.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.public")
data class AppProperties(
    val baseUrl: String = "http://localhost:3000",
)
