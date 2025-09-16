package com.architect.risk.management.service;

import com.architect.risk.management.client.AccountSummaryClient;
import com.architect.risk.management.resource.external.dto.AccountSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class BalanceMonitorService {

    private final AccountSummaryClient accountSummaryClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public BalanceMonitorService(AccountSummaryClient accountSummaryClient) {
        this.accountSummaryClient = accountSummaryClient;
    }

    public AccountSummaryDTO getAccountSummaryByAccountId(String accountId) {
        try {
            return accountSummaryClient.getRawAccountSummary(accountId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar AccountSummary", e);
        }
    }

}
