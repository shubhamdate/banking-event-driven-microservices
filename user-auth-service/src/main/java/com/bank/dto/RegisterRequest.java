package com.bank.dto;

import com.bank.enums.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotNull
    private Role role;
    @Nullable    private String firstName;
    @Nullable
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
    @Pattern(regexp = "^\\d{10}$")
    private String mobile;
}
