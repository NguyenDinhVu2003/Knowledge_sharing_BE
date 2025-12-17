package com.company.knowledge_sharing_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // Security scheme for JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Knowledge Sharing Platform API")
                        .version("1.0.0")
                        .description("""
                                # Knowledge Sharing Platform API Documentation
                                
                                This is a comprehensive REST API for an internal knowledge sharing platform.
                                
                                ## Features
                                - **Authentication:** JWT-based authentication and authorization
                                - **Document Management:** Upload, update, version control, and share documents
                                - **Rating & Favorites:** Rate documents and manage favorites
                                - **Notifications:** Real-time notifications for relevant activities
                                - **Search:** Advanced search with multiple filters and facets
                                - **Tag & Group Management:** Organize documents with tags and groups
                                - **Admin Panel:** User management and system statistics
                                - **User Interests:** Personalized notifications based on interests
                                
                                ## Authentication
                                Most endpoints require JWT authentication. 
                                1. Register or login to get a JWT token
                                2. Click "Authorize" button and enter:  `Bearer YOUR_TOKEN`
                                3. All subsequent requests will include the token
                                
                                ## Base URL
                                - Development: `http://localhost:8080/api`
                                - Production: Configure in deployment
                                """)
                        .contact(new Contact()
                                .name("Knowledge Sharing Team")
                                .email("support@knowledgesharing.com")
                                .url("https://github.com/yourorg/knowledge-sharing"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.knowledgesharing.com/api")
                                .description("Production Server (if applicable)")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}

