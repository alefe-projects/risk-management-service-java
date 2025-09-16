package com.architect.risk.management.service;

import com.architect.risk.management.db.model.UserConfiguration;
import com.architect.risk.management.db.repository.UserConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserConfigurationService {

    private final UserConfigRepository userConfigRepository;

    public UserConfigurationService(UserConfigRepository userConfigRepository) {
        this.userConfigRepository = userConfigRepository;
    }

    public UserConfiguration saveUserConfig(UserConfiguration userConfiguration) {
        return userConfigRepository.save(userConfiguration);
    }

    public List<UserConfiguration> getAllConfigurations() {
        return userConfigRepository.findAll();
    }

}
