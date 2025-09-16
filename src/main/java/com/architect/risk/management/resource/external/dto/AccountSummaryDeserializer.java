package com.architect.risk.management.resource.external.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AccountSummaryDeserializer extends StdDeserializer<AccountSummaryDTO> {

    public AccountSummaryDeserializer() {
        this(null);
    }

    public AccountSummaryDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AccountSummaryDTO deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        String raw = jp.getValueAsString();

        if (raw == null || !raw.startsWith("AccountSummary(")) {
            return null;
        }

        Map<String, String> fields = getStringStringMap(raw);

        String account = fields.get("account");
        BigDecimal equity = parseDecimal(fields.get("equity"));
        BigDecimal cashExcess = parseDecimal(fields.get("cash_excess"));
        BigDecimal positionMargin = parseDecimal(fields.get("position_margin"));
        BigDecimal purchasingPower = parseDecimal(fields.get("purchasing_power"));
        BigDecimal realizedPnl = parseDecimal(fields.get("realized_pnl"));
        BigDecimal totalMargin = parseDecimal(fields.get("total_margin"));
        BigDecimal unrealizedPnl = parseDecimal(fields.get("unrealized_pnl"));
        BigDecimal yesterdayEquity = parseDecimal(fields.get("yesterday_equity"));
        OffsetDateTime timestamp = parseDate(fields.get("timestamp"));

        Map<String, BigDecimal> balances = new HashMap<>();
        if (fields.get("balances") != null && fields.get("balances").contains("USD")) {
            balances.put("USD", parseDecimal(fields.get("balances").replaceAll("[^0-9.]", "")));
        }

        return new AccountSummaryDTO(
                account,
                balances,
                Collections.emptyMap(),
                timestamp,
                cashExcess,
                equity,
                positionMargin,
                purchasingPower,
                realizedPnl,
                totalMargin,
                unrealizedPnl,
                yesterdayEquity
        );
    }

    private static Map<String, String> getStringStringMap(String raw) {
        String content = raw.substring("AccountSummary(".length(), raw.length() - 1);

        Map<String, String> fields = new LinkedHashMap<>();
        int depth = 0;
        StringBuilder keyVal = new StringBuilder();
        String currentKey = null;

        for (char c : content.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') depth++;
            if (c == ')' || c == '}' || c == ']') depth--;

            if (c == '=' && depth == 0 && currentKey == null) {
                currentKey = keyVal.toString().trim();
                keyVal.setLength(0);
            } else if (c == ',' && depth == 0 && currentKey != null) {
                fields.put(currentKey, keyVal.toString().trim());
                currentKey = null;
                keyVal.setLength(0);
            } else {
                keyVal.append(c);
            }
        }
        if (currentKey != null) {
            fields.put(currentKey, keyVal.toString().trim());
        }
        return fields;
    }

    private BigDecimal parseDecimal(String val) {
        if (val == null || val.equals("None") || val.equals("null")) return null;
        try {
            return new BigDecimal(val.replace("Decimal(", "").replace(")", "").replace("'", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private OffsetDateTime parseDate(String val) {
        if (val == null || val.equals("None") || val.equals("null")) return null;
        try {
            return OffsetDateTime.parse(val.trim(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}