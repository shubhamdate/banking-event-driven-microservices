package com.example.accounts.resources;

import com.example.accounts.dto.BalanceOperationRequest;
import com.example.accounts.service.AccountBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/balance")
@RequiredArgsConstructor
public class InternalBalanceController {

    private final AccountBalanceService balanceService;

    @PostMapping("/debit")
    //@PreAuthorize("hasRole('SYSTEM')")
    public void debit(
            @Valid @RequestBody BalanceOperationRequest request) {
        balanceService.debit(request);
    }

    @PostMapping("/credit")
    //@PreAuthorize("hasRole('SYSTEM')")
    public void credit(@Valid @RequestBody BalanceOperationRequest request) {
        balanceService.credit(request);
    }
}
