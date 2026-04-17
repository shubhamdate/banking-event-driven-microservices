package com.bank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String mobile;
    private boolean active;
}
