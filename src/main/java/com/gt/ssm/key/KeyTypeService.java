package com.gt.ssm.key;

import com.gt.ssm.config.CachingConfig;
import com.gt.ssm.model.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeyTypeService {

    private static final Logger log = LoggerFactory.getLogger(KeyTypeService.class);

    private final KeyTypeDao keyTypeDao;

    public KeyTypeService(KeyTypeDao keyTypeDao) {
        this.keyTypeDao = keyTypeDao;
    }

    @Cacheable(CachingConfig.KEY_TYPES)
    public List<KeyType> getAllKeyTypes() {
        return keyTypeDao.getAllKeyTypes();
    }

    @Cacheable(CachingConfig.KEY_TYPES_BY_ID)
    public Map<String, KeyType> getAllKeyTypesById() {
        return getAllKeyTypes().stream().collect(Collectors.toMap(kt -> kt.id(), kt -> kt));
    }

    public KeyType getKeyTypeById(String id) {
        return getAllKeyTypesById().get(id);
    }
}
