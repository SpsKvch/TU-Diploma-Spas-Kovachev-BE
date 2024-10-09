package com.test.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {

    @Bean
    public OpenAPI bearerConfig() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                    .scheme("bearer")
                                                    .type(SecurityScheme.Type.HTTP)
                                                    .bearerFormat("jwt")
                                                    .name("Auth")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        return openAPI;
    }

}
