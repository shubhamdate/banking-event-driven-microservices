package com.example.accounts.service.impl;

import com.example.accounts.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {

    private static final String BANK_CODE = "HDFC";
    private static final String BRANCH_CODE = "0012";

    private final AccountRepository accountRepository;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String generateAccountNumber() {

        Long sequence = accountRepository.getNextSequence();

        String sequencePart = String.format("%012d", sequence);

        String base = BANK_CODE + BRANCH_CODE + sequencePart;

        int checksum = calculateChecksum(base);

        return base + checksum;
    }

    public boolean isValidAccountNumber(String accountNumber) {

        String base = accountNumber.substring(0, accountNumber.length() - 1);
        int expectedChecksum = Character.getNumericValue(
                accountNumber.charAt(accountNumber.length() - 1)
        );

        return calculateChecksum(base) == expectedChecksum;
    }

    private int calculateChecksum(String input) {

        // Convert letters → numbers (A=10, B=11...)
        StringBuilder numeric = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                numeric.append(Character.getNumericValue(c));
            }
        }

        String number = numeric.toString();

        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';

            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }

            sum += n;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }
}
