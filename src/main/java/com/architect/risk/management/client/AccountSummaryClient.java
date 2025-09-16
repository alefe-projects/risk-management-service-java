package com.architect.risk.management.client;

import com.architect.risk.management.resource.external.dto.AccountSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AccountSummaryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public AccountSummaryDTO getRawAccountSummary(String accountId) {
        String url = "http://localhost/account_summary/" + accountId;
        return restTemplate.getForObject(url, AccountSummaryDTO.class);
    }
}