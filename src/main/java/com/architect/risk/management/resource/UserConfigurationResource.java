package com.architect.risk.management.resource;

import com.architect.risk.management.db.model.UserConfiguration;
import com.architect.risk.management.service.UserConfigurationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk-configs")
public class UserConfigurationResource {

    private final UserConfigurationService userConfigurationService;

    public UserConfigurationResource(UserConfigurationService userConfigurationService) {
        this.userConfigurationService = userConfigurationService;
    }

    @PostMapping
    public ResponseEntity<?> createRiskConfig(@RequestBody UserConfiguration userConfiguration) {
        try {
            UserConfiguration userConfigurationSaved = userConfigurationService.saveUserConfig(userConfiguration);
            return ResponseEntity.ok(userConfigurationSaved);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.badRequest()
                    .body("Duplicate key: clientId, apiKey or apiSecret must be unique.");
        }
    }

}
