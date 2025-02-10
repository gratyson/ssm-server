package com.gt.ssm.config;

import com.gt.ssm.crypt.AESEncryptionService;
import com.gt.ssm.crypt.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BeanConfig {

    @Bean
    public EncryptionService getEncryptionService(
            AESEncryptionService aesEncryptionService,
            @Value("${ssm.encryption.encryptAlgorithm}") String encryptAlgorithm) {
        return new EncryptionService(
                List.of(aesEncryptionService)
                , encryptAlgorithm);
    }
}
