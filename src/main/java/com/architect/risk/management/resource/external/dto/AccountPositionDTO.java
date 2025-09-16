package com.architect.risk.management.resource.external.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AccountPositionDTO(
        BigDecimal quantity,
        BigDecimal break_even_price,
        BigDecimal cost_basis,
        BigDecimal liquidation_price,
        OffsetDateTime trade_time,
        BigDecimal unrealized_pnl
) {}