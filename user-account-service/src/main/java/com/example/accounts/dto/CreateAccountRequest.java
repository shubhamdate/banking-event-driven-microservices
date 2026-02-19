package com.example.accounts.dto;

import com.example.accounts.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateAccountRequest {
    @NotNull
    private AccountType accountType; // SAVINGS / CURRENT / WALLET
    @NotBlank
    private String email;
    @NotBlank
    private String mobile;
}