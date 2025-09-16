package com.architect.risk.management.db.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Document(collection = "risk_blocked_users")
public record RiskBlockedUser(
        @Id
        String id,
        @Indexed(unique = true)
        String clientId,
        String riskType,
        String status,
        OffsetDateTime blockedAt,
        OffsetDateTime unblockAt
) {}