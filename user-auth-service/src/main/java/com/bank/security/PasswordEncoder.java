package com.bank.security;

public interface PasswordEncoder {

    String hash(String plainPassword);

    boolean matches(String plainPassword, String hashedPassword);
}
