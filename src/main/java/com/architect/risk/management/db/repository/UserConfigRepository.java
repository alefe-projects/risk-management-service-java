package com.architect.risk.management.db.repository;

import com.architect.risk.management.db.model.UserConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserConfigRepository extends MongoRepository<UserConfiguration, String> {
}
