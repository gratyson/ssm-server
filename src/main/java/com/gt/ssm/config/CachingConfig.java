package com.gt.ssm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class CachingConfig {

    private static final Logger log = LoggerFactory.getLogger(CachingConfig.class);

    public static final String SECRET_TYPES = "secret_types";
    public static final String SECRET_TYPES_BY_ID = "secret_types_by_id";

    public static final String KEY_TYPES = "key_types";
    public static final String KEY_TYPES_BY_ID = "key_types_by_id";

    public static final String COMPONENT_TYPES = "component_types";
    public static final String COMPONENTS_TYPES_BY_ID = "components_by_id";
    public static final String COMPONENT_TYPES_BY_QL_NAME = "component_types_by_ql_name";

    private static final long CACHE_EVICT_SCHEDULE_MS = 4 * 60 * 60 * 1000;

    @Bean
    public CacheManager getLanguageCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(SECRET_TYPES, SECRET_TYPES_BY_ID, KEY_TYPES, KEY_TYPES_BY_ID, COMPONENT_TYPES, COMPONENTS_TYPES_BY_ID, COMPONENT_TYPES_BY_QL_NAME);
        return cacheManager;
    }

    @CacheEvict(allEntries = true, value = { SECRET_TYPES, SECRET_TYPES_BY_ID, KEY_TYPES, KEY_TYPES_BY_ID, COMPONENT_TYPES, COMPONENTS_TYPES_BY_ID, COMPONENT_TYPES_BY_QL_NAME})
    @Scheduled(fixedDelay = CACHE_EVICT_SCHEDULE_MS,  initialDelay = CACHE_EVICT_SCHEDULE_MS)
    public void ReportLanguageCacheEvict() {
        log.info("Flushing caches.");
    }
}
