package com.bank.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class JwtTokenFactory {

    public String generateToken(String username, Long userId, String role, String email, String mobile) {
        return Jwt.issuer("bank-auth-service")
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("email", email)
                .claim("mobile", mobile)
                .groups(Set.of(role))
                .expiresIn(600)
                .sign();
    }
}
