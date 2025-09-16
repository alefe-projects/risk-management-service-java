package com.architect.risk.management.db.repository;

import com.architect.risk.management.db.model.RiskBlockedUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RiskBlockedUserRepository  extends MongoRepository<RiskBlockedUser, String> {

    Optional<RiskBlockedUser> findByClientIdAndRiskType(String clientId, String riskType);

}
