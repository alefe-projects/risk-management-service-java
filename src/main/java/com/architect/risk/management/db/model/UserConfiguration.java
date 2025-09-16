package com.architect.risk.management.db.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "userConfig")
public record UserConfiguration(
        @Id String id,
        @Indexed(unique = true)
        String clientId,
        @Indexed(unique = true)
        String apiKey,
        @Indexed(unique = true)
        String apiSecret,
        RiskSetting maxRisk,
        RiskSetting dailyRisk
) {}
