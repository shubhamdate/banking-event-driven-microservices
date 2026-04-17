package com.bank.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BCryptPasswordEncoder implements PasswordEncoder {

    @Override
    public String hash(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
