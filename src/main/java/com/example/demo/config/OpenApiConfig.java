package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fitTrackOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("FitTrack AI — REST API")
                        .description("""
                                Intelligent fitness platform API powered by Spring Boot and Google Gemini AI.
                                
                                ## Features
                                - **Authentication** — JWT-based stateless auth (register, login)
                                - **Exercise Logging** — Full CRUD with pagination, quick-log, and analytics
                                - **AI Workout Generation** — Context-aware plans via Google Gemini with fallback system
                                - **Nutrition Tracking** — USDA FoodData Central integration with customizable macro goals
                                - **Analytics** — Volume trends, exercise distribution, streak tracking
                                
                                ## Authentication
                                1. Call `POST /api/users/login` with your credentials
                                2. Copy the `jwt` value from the response
                                3. Click the **Authorize** button above and paste: `Bearer <your_token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Himanshu Yadav")
                                .url("https://github.com/1himanshu1804442"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Get one from POST /api/users/login")));
    }
}
