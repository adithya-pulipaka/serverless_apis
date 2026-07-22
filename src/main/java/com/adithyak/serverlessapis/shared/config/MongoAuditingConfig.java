package com.adithyak.serverlessapis.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@ConditionalOnBean(MongoMappingContext.class)
@EnableMongoAuditing
public class MongoAuditingConfig {
}
