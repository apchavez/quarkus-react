package com.products.adapters.in.rest;

import com.products.application.dto.LoginRequest;
import com.products.application.dto.LoginResponse;
import com.products.application.port.in.AuthServicePort;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Auth", description = "Authentication — issues JWTs for the demo user store")
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AuthResource {

    @Inject
    AuthServicePort authService;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login", description = "Authenticates a demo user and returns a signed JWT. Demo credentials: admin/admin123 (ADMIN), user/user123 (USER).")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Login successful — body contains the JWT"),
        @APIResponse(responseCode = "400", description = "Validation error on request fields"),
        @APIResponse(responseCode = "401", description = "Invalid username or password")
    })
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }
}
