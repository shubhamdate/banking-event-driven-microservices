package com.bank.entity;

import com.bank.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(
        name = "BANK_USERS", schema = "dbo"
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "FirstName", length = 50)
    private String firstName;

    @Column(name = "LastName", length = 50)
    private String lastName;

    @Column(name = "Role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "Email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mobile", nullable = false, unique = true, length = 20)
    private String mobile;

    @Column(name = "Active", nullable = false)
    private boolean active;

    @Column(name = "CreatedBy", nullable = true, length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private Timestamp updatedAt;
}