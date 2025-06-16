package com.gt.ssm.crypt;

import com.google.crypto.tink.subtle.Base64;
import com.gt.ssm.exception.SecretEncryptionException;
import org.springframework.beans.factory.BeanInitializationException;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EncryptionService {

    private static final String CHARSET_NAME = "UTF-8";

    private final Map<String, EncryptionServiceForAlgorithm> encryptionServiceByAlgorithm;
    private final String encryptAlgorithm;

    public EncryptionService(List<EncryptionServiceForAlgorithm> encryptionServicesForAlgorithms, String encryptAlgorithm) {
        this.encryptionServiceByAlgorithm = Collections.unmodifiableMap(encryptionServicesForAlgorithms
                .stream()
                .collect(Collectors.toMap(
                        EncryptionServiceForAlgorithm::getAlgorithm,
                        encryptionServiceForAlgorithm -> encryptionServiceForAlgorithm)));

        this.encryptAlgorithm = encryptAlgorithm;

        if (!encryptionServiceByAlgorithm.containsKey(this.encryptAlgorithm)) {
            throw new BeanInitializationException("No encryption bean exists for the default encryption algorithm.");
        }
    }

    public String getCurrentEncryptAlgorithm() {
        return encryptAlgorithm;
    }

    public byte[] getKeyFromPassword(String keyPassword, String salt, String algorithm) {
        return getEncryptionService(algorithm).getKeyFromPassword(keyPassword, salt);
    }

    public String encrypt(String data, byte[] keyBytes, String iv) {
        return Base64.encode(encrypt(data.getBytes(Charset.forName(CHARSET_NAME)), keyBytes, iv));
    }

    public byte[] encrypt(byte[] data, byte[] keyBytes, String iv) {
        return getEncryptionService(encryptAlgorithm).encrypt(data, keyBytes, iv);
    }

    public String decrypt(String encryptedData, byte[] keyBytes, String iv, String algorithm) {
        return new String(decrypt(Base64.decode(encryptedData), keyBytes, iv, algorithm), Charset.forName(CHARSET_NAME));
    }

    public byte[] decrypt(byte[] encryptedData, byte[] keyBytes, String iv, String algorithm) {
        return getEncryptionService(algorithm).decrypt(encryptedData, keyBytes, iv);
    }

    private EncryptionServiceForAlgorithm getEncryptionService(String algorithm) {
        EncryptionServiceForAlgorithm encryptionServiceForAlgorithm = encryptionServiceByAlgorithm.get(algorithm);

        if (encryptionServiceForAlgorithm == null) {
            throw new SecretEncryptionException("'" + algorithm + "' algorithm not supported.");
        }

        return encryptionServiceForAlgorithm;
    }
}
