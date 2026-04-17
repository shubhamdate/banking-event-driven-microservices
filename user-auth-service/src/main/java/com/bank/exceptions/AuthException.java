package com.bank.exceptions;

import com.bank.dto.AuthError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthException extends RuntimeException {

    private final AuthError authError;

    public AuthException(AuthError authError) {
        super(authError.getDescriptionError());
        this.authError = authError;
    }

    public AuthError getAuthError() {
        return authError;
    }
}