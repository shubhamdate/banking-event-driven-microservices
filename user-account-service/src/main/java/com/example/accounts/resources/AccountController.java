package com.example.accounts.resources;

import com.example.accounts.dto.AccountResponse;
import com.example.accounts.dto.AccountStatusResponse;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.enums.AccountStatus;
import com.example.accounts.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public AccountResponse createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return accountService.createAccount(jwt.getSubject(),
                request
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<AccountResponse> getMyAccounts(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return accountService.getMyAccounts(jwt.getSubject());
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public AccountResponse getAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String accountNumber
    ) {
        return accountService.getAccountByNumber(
                jwt.getSubject(),
                accountNumber
        );
    }

    @PatchMapping("/{accountNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountStatusResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String accountNumber,
            @RequestParam AccountStatus status
    ) {
        return accountService.changeAccountStatus(
                jwt.getSubject(),
                accountNumber,
                status
        );
    }

}
