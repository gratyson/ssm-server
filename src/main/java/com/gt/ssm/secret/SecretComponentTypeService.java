package com.gt.ssm.secret;

import com.gt.ssm.config.CachingConfig;
import com.gt.ssm.model.SecretComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SecretComponentTypeService {

    private static final Logger log = LoggerFactory.getLogger(SecretComponentTypeService.class);

    private final SecretComponentTypeDao secretComponentTypeDao;

    public SecretComponentTypeService(SecretComponentTypeDao secretComponentTypeDao) {
        this.secretComponentTypeDao = secretComponentTypeDao;
    }

    @Cacheable(CachingConfig.COMPONENT_TYPES)
    public List<SecretComponentType> getAllSecretComponentTypes() {
        return secretComponentTypeDao.getAllSecretComponentTypes();
    }

    @Cacheable(CachingConfig.COMPONENTS_TYPES_BY_ID)
    private Map<String, SecretComponentType> getAllSecretTypesById() {
        return getAllSecretComponentTypes().stream().collect(Collectors.toMap(SecretComponentType::id, secretComponentType -> secretComponentType));
    }

    @Cacheable(CachingConfig.COMPONENT_TYPES_BY_QL_NAME)
    public Map<String, SecretComponentType> getAllSecretTypesByQlName() {
        return getAllSecretComponentTypes().stream().collect(Collectors.toMap(SecretComponentType::qlName, secretComponentType -> secretComponentType));
    }

    public SecretComponentType getComponentByTypeId(String id) {
        return getAllSecretTypesById().get(id);
    }

    public SecretComponentType getComponentByTypeQlName(String qlName) {
        return getAllSecretTypesByQlName().get(qlName);
    }
}
