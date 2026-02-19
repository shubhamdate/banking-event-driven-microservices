package com.bank.exceptions;

import com.bank.dto.AuthError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthException extends Exception {

    private final AuthError authError;

    private final Exception parentException;

    public AuthException(AuthError authError, Exception parentException) {
        this.authError = authError;
        this.parentException = parentException;
    }

    public AuthException(AuthError authError) {
        this.authError = authError;
        this.parentException = null;
    }
}