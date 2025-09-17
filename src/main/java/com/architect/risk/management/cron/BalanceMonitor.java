package com.architect.risk.management.cron;

import com.architect.risk.management.db.model.RiskBlockedUser;
import com.architect.risk.management.db.model.RiskSetting;
import com.architect.risk.management.db.model.UserConfiguration;
import com.architect.risk.management.db.repository.RiskBlockedUserRepository;
import com.architect.risk.management.resource.external.dto.AccountSummaryDTO;
import com.architect.risk.management.service.BalanceMonitorService;
import com.architect.risk.management.service.UserConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BalanceMonitor {

    private final UserConfigurationService userConfigurationService;
    private final BalanceMonitorService balanceMonitorService;
    private final RiskBlockedUserRepository riskBlockedUserRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BalanceMonitor(
            UserConfigurationService userConfigurationService,
            BalanceMonitorService balanceMonitorService,
            RiskBlockedUserRepository riskBlockedUserRepository
    ) {
        this.userConfigurationService = userConfigurationService;
        this.balanceMonitorService = balanceMonitorService;
        this.riskBlockedUserRepository = riskBlockedUserRepository;
    }

    @Scheduled(fixedRate = 500)
    public void monitorBalance() {
        List<UserConfiguration> configs = userConfigurationService.getAllConfigurations();

        configs.parallelStream().forEach(config -> {
            AccountSummaryDTO accountSummary = balanceMonitorService.getAccountSummaryByAccountId(config.clientId());
            processDailyLoss(accountSummary, config);
            processMaxLoss(accountSummary, config);
        });
    }

    private void processDailyLoss(AccountSummaryDTO accountSummary, UserConfiguration userConfiguration) {
        RiskSetting dailyRiskSetting = userConfiguration.dailyRisk();

        BigDecimal dailyLossLimit;

        if ("absolute".equalsIgnoreCase(dailyRiskSetting.type())) {
            dailyLossLimit = BigDecimal.valueOf(dailyRiskSetting.value());
        } else if ("percentage".equalsIgnoreCase(dailyRiskSetting.type())) {
            dailyLossLimit = accountSummary.equity()
                    .multiply(BigDecimal.valueOf(dailyRiskSetting.value()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
        } else {
            throw new IllegalArgumentException("Invalid dailyRisk type: " + dailyRiskSetting.type());
        }

        BigDecimal realizedLoss = accountSummary.realized_pnl().negate();

        if (realizedLoss.compareTo(dailyLossLimit) >= 0) {
            blockTradingUntilNextDay(userConfiguration.clientId());
            sendDailyRiskNotification(userConfiguration.clientId(), realizedLoss, dailyLossLimit);
        }
    }

    private void processMaxLoss(AccountSummaryDTO accountSummary, UserConfiguration userConfiguration) {
        RiskSetting maxRiskSetting = userConfiguration.maxRisk();
        BigDecimal maxLossLimit;
        BigDecimal equity = accountSummary.equity();

        if ("absolute".equalsIgnoreCase(maxRiskSetting.type())) {
            maxLossLimit = BigDecimal.valueOf(maxRiskSetting.value());
        } else if ("percentage".equalsIgnoreCase(maxRiskSetting.type())) {
            maxLossLimit = equity
                    .multiply(BigDecimal.valueOf(maxRiskSetting.value()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
        } else {
            throw new IllegalArgumentException("Invalid dailyRisk type: " + maxRiskSetting.type());
        }

        BigDecimal totalLoss = equity.subtract(accountSummary.equity());

        if (totalLoss.compareTo(maxLossLimit) >= 0) {
            permanentlyBlockTrading(userConfiguration.clientId());
            sendMaxRiskNotification(userConfiguration.clientId(), totalLoss, maxLossLimit);
        }
    }

    private void blockTradingUntilNextDay(String clientId) {
        OffsetDateTime now = OffsetDateTime.now();

        var optionalBlockedUser = riskBlockedUserRepository.findByClientIdAndRiskType(clientId, "DAILY");

        if (optionalBlockedUser.isPresent()) {
            RiskBlockedUser blockedUser = optionalBlockedUser.get();

            if ("BLOCKED".equals(blockedUser.status()) && blockedUser.unblockAt() != null && now.isBefore(blockedUser.unblockAt())) {
                return;
            }

            if ("BLOCKED".equals(blockedUser.status()) && blockedUser.unblockAt() != null && now.isAfter(blockedUser.unblockAt())) {
                RiskBlockedUser unblocked = new RiskBlockedUser(
                        blockedUser.id(),
                        clientId,
                        "DAILY",
                        "UNBLOCKED",
                        blockedUser.blockedAt(),
                        blockedUser.unblockAt()
                );
                riskBlockedUserRepository.save(unblocked);
            }
        }

        OffsetDateTime tomorrow = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        RiskBlockedUser newBlock = new RiskBlockedUser(
                null,
                clientId,
                "DAILY",
                "BLOCKED",
                now,
                tomorrow
        );
        riskBlockedUserRepository.save(newBlock);
    }

    private void sendDailyRiskNotification(String clientId, BigDecimal loss, BigDecimal limit) {
        try {
        ObjectNode eventNode = objectMapper.createObjectNode();
        eventNode.put("clientId", clientId);
        eventNode.put("event", "DAILY_RISK_TRIGGERED");
        eventNode.put("loss", loss);
        eventNode.put("limit", limit);

        String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        eventNode.put("time", now);

        eventNode.put("action", "All positions closed. Trading disabled for today.");

        String jsonEvent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventNode);
        System.out.println(jsonEvent);
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    private void permanentlyBlockTrading(String clientId) {
        OffsetDateTime now = OffsetDateTime.now();

        RiskBlockedUser blockedUser = new RiskBlockedUser(
                null,
                clientId,
                "MAX",
                "BLOCKED",
                now,
                null
        );
        riskBlockedUserRepository.save(blockedUser);
    }
    private void sendMaxRiskNotification(String clientId, BigDecimal loss, BigDecimal limit) {
        try {
            ObjectNode eventNode = objectMapper.createObjectNode();
            eventNode.put("clientId", clientId);
            eventNode.put("event", "MAX_RISK_TRIGGERED");
            eventNode.put("loss", loss);
            eventNode.put("limit", limit);

            String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            eventNode.put("time", now);

            eventNode.put("action", "All positions closed. Trading permanently disabled for this user.");

            String jsonEvent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventNode);

            System.out.println(jsonEvent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
