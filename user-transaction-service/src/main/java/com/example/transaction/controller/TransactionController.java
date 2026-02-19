package com.example.transaction.controller;

import com.example.transaction.dto.TransferRequest;
import com.example.transaction.dto.TransferResponse;
import com.example.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody TransferRequest request
    ) {
        return transactionService.transfer(token, request);
    }
}