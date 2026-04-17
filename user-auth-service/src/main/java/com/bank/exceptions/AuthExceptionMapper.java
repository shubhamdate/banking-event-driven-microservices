package com.bank.exceptions;

import com.bank.dto.AuthError;
import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

@Provider
public class AuthExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {

        Response response;
        AuthError authError;

        switch (e) {
            case AuthException authException -> {
                authError = authException.getAuthError();
                authError.setHttpStatusCode(Optional.ofNullable(authError.getHttpStatusCode()).orElse(500));
                response = buildResponseFromError(authError);
            }
            case WebApplicationException webApplicationException -> response = webApplicationException.getResponse();
            default -> {
                authError = buildDefaultAuthError(e);
                response = buildResponseFromError(authError);
            }
        }

        Log.error(e.getMessage(), e);
        return response;
    }

    private Response buildResponseFromError(AuthError authError) {
        return Response.status(mapErrorCodeToHttpStatus(authError.getHttpStatusCode()))
                .entity(authError)
                .build();
    }

    private AuthError buildDefaultAuthError(Exception e) {
        return AuthError.builder()
                .httpStatusCode(500)
                .codigoError("Error")
                .descriptionError(Optional.ofNullable(e.getMessage()).orElse("Unknown Error"))
                .build();
    }

    private Response.Status mapErrorCodeToHttpStatus(int errorCode) {
        return switch (errorCode) {
            case 400 -> Response.Status.BAD_REQUEST;
            case 401 -> Response.Status.UNAUTHORIZED;
            case 403 -> Response.Status.FORBIDDEN;
            case 404 -> Response.Status.NOT_FOUND;
            case 405 -> Response.Status.METHOD_NOT_ALLOWED;
            case 429 -> Response.Status.TOO_MANY_REQUESTS;
            case 502 -> Response.Status.BAD_GATEWAY;
            case 503 -> Response.Status.SERVICE_UNAVAILABLE;
            case 504 -> Response.Status.GATEWAY_TIMEOUT;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }
}