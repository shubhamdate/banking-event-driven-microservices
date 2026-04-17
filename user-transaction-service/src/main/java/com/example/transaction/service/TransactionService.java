package com.example.transaction.service;

import com.example.transaction.dto.TransferRequest;
import com.example.transaction.dto.TransferResponse;

public interface TransactionService {

    TransferResponse transfer(TransferRequest request);

    TransferResponse getTransaction(String transactionRef);
}
