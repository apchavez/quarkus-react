package com.products;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "Product Management API",
        version = "1.0.0",
        description = "REST API for managing products. Supports CRUD, search by SKU, name prefix search and pagination.",
        contact = @Contact(
            name = "Alexander Prieto Chavez",
            email = "alexander.prieto.chavez@gmail.com",
            url = "https://github.com/apchavez"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local"),
        @Server(url = "https://api.products.example.com", description = "Production")
    }
)
@SecurityScheme(
    securitySchemeName = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT issued by the system. Available roles: ADMIN (full access), USER (read-only)."
)
public class OpenApiConfig extends Application {
}
