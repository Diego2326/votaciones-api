package com.votaciones.api.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearer-jwt"
        return OpenAPI()
            .info(
                Info()
                    .title("Votaciones API")
                    .description(
                        "API REST para torneos, encuestas y brackets competitivos con autenticacion JWT, auditoria y eventos en tiempo real.",
                    )
                    .version("v1")
                    .contact(
                        Contact()
                            .name("Votaciones API")
                            .email("backend@votaciones.local"),
                    )
                    .license(
                        License()
                            .name("Proprietary")
                            .url("https://example.com/license"),
                    ),
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .`in`(SecurityScheme.In.HEADER),
                ),
            )
    }
}
