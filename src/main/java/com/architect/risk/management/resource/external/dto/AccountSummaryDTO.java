package com.architect.risk.management.resource.external.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AccountSummaryDTO(
        String account,
        Map<String, BigDecimal> balances,
        Map<String, List<AccountPositionDTO>> positions,
        OffsetDateTime timestamp,
        BigDecimal cash_excess,
        BigDecimal equity,
        BigDecimal position_margin,
        BigDecimal purchasing_power,
        BigDecimal realized_pnl,
        BigDecimal total_margin,
        BigDecimal unrealized_pnl,
        BigDecimal yesterday_equity
) {}

