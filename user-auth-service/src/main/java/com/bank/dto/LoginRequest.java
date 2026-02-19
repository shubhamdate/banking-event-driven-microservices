package com.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotBlank
    public String username;
    @NotBlank
    public String password;
}
