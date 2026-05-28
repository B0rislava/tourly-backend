package com.tourly.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"
        return OpenAPI()
            .info(
                Info()
                    .title("Tourly API")
                    .version("1.0")
                    .description("""
                        API documentation for the Tourly application.
                        
                        **Developer:** Borislava Ivanova
                        *   **Backend:** [tourly-backend](https://github.com/B0rislava/tourly-backend)
                        *   **Android:** [tourly-android](https://github.com/B0rislava/tourly-android)
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("Borislava Ivanova")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
}
