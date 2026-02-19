package com.bank.resource;

import com.bank.dto.ApiResponse;
import com.bank.dto.ChangePasswordRequest;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.exceptions.AuthException;
import com.bank.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterRequest request) throws AuthException {
        ApiResponse apiResponse = authService.register(request);
        return Response.status(Response.Status.CREATED)
                .entity(apiResponse)
                .build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) throws AuthException {

        return Response.ok()
                .entity(authService.login(request))
                .build();
    }

    @PATCH
    @Path("/users/{id}/status")
    @RolesAllowed("ADMIN")
    public Response changeUserStatus(@PathParam("id") Long id, @QueryParam("active") boolean active) throws AuthException {

        return Response.ok()
                .entity(authService.changeUserStatus(id, active))
                .build();
    }

    @PATCH
    @Path("/change-password")
    public Response changePassword(@Valid ChangePasswordRequest changePasswordRequest) throws AuthException {

        return Response.ok()
                .entity(authService.changePassword(changePasswordRequest))
                .build();
    }

    @POST
    @Path("/refresh-token")
    public Response refreshToken() {
        return Response.ok()
                .entity(authService.refreshToken())
                .build();
    }

    @GET
    @Path("/get-profile")
    public Response getProfile() throws AuthException {

        return Response.ok()
                .entity(authService.getProfile())
                .build();
    }
}

