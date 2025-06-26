package com.eduhub.auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // Metadata shown in Swagger UI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduHub Auth Service API")
                        .description("API documentation for authentication and authorization endpoints.")
                        .version("1.0.0")
                        .contact(new Contact().name("EduHub Dev Team").email("support@eduhub.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }

    // Optional: Grouping APIs (if you want multiple modules grouped)
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("auth-api")
                .pathsToMatch("/auth/**")
                .build();
    }
}
