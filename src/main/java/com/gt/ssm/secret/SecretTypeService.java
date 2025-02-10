package com.gt.ssm.secret;

import com.gt.ssm.config.CachingConfig;
import com.gt.ssm.model.SecretType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SecretTypeService {

    private static final Logger log = LoggerFactory.getLogger(SecretTypeService.class);

    private final SecretTypeDao secretTypeDao;

    @Autowired
    public SecretTypeService(SecretTypeDao secretTypeDao) {
        this.secretTypeDao = secretTypeDao;
    }

    @Cacheable(CachingConfig.SECRET_TYPES)
    public List<SecretType> getAllSecretTypes() {
        return secretTypeDao.getAllSecretTypes();
    }

    @Cacheable(CachingConfig.SECRET_TYPES_BY_ID)
    public Map<String, SecretType> getAllSecretTypesById() {
        return getAllSecretTypes().stream().collect(Collectors.toMap(st -> st.id(), st -> st));
    }

    public SecretType getSecretTypeById(String id) {
        return getAllSecretTypesById().get(id);
    }
}
