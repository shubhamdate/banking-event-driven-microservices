package com.bank.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class JwtTokenFactory {


    public String generateToken(String username, Long userId, String role) {
        return Jwt.issuer("bank-auth-service")
                .subject(String.valueOf(userId))   // sub MUST be String
                .claim("username", username)
                .groups(Set.of(role))
                .expiresIn(60000000)
                .sign();
    }

}
