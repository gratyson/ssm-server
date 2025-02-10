package com.gt.ssm.crypt;

import com.google.crypto.tink.subtle.Base64;
import com.gt.ssm.exception.KeyException;
import com.gt.ssm.key.KeyDao;
import com.gt.ssm.key.KeyService;
import com.gt.ssm.crypt.model.EncryptedKey;
import com.gt.ssm.model.Key;
import com.gt.ssm.security.LocalUserDetailsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

@Component
public class EncryptedKeyService {

    private static final Logger log = LoggerFactory.getLogger(EncryptedKeyService.class);

    private static final String DIRECT_LOCK_KEY_TYPE = "direct_lock";
    private static final int KEY_SIZE = 256;
    private static final int IV_LENGTH = 16;

    private final EncryptedKeyDao encryptedKeyDao;
    private final EncryptionService encryptionService;
    private final KeyDao keyDao;
    private final LocalUserDetailsManager localUserDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public EncryptedKeyService(EncryptedKeyDao encryptedKeyDao,
                               EncryptionService encryptionService,
                               KeyDao keyDao,
                               LocalUserDetailsManager localUserDetailsManager,
                               PasswordEncoder passwordEncoder) {
        this.encryptedKeyDao = encryptedKeyDao;
        this.encryptionService = encryptionService;
        this.keyDao = keyDao;
        this.localUserDetailsManager = localUserDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    public byte[] getKey(String owner, String keyId, String keyPassword) {
        Key key = getKey(owner, keyId);

        validateKeyPassword(owner, key, keyPassword);

        if (key != null && key.typeId().equals(DIRECT_LOCK_KEY_TYPE)) {
            return encryptionService.getKeyFromPassword(keyPassword, key.salt(), key.algorithm());
        }

        return getOrCreateEncryptedKey(owner, key, keyPassword);
    }

    private Key getKey(String owner, String keyId) {
        if (keyId == null || keyId.equals("") || keyId.equals(KeyService.USER_PASSWORD_KEY_ID)) {
            return null;
        } else {
            Key key = keyDao.loadKey(keyId, owner);

            if (key == null) {
                log.warn("Key {} not found", keyId);
                throw new KeyException("Key not found");
            }

            return key;
        }
    }

    private void validateKeyPassword(String owner, Key key, String keyPassword) {
        String keyPasswordHash =
                key == null ? localUserDetailsManager.loadUserByUsername(owner).getPassword() : key.keyPassword();

        if (!passwordEncoder.matches(keyPassword, keyPasswordHash)) {
            throw new KeyException("Key password is incorrect");
        }
    }

    private byte[] getOrCreateEncryptedKey(String owner, Key key, String keyPassword) {
        String salt;
        EncryptedKey encryptedKey;
        if (key == null) {
            encryptedKey = encryptedKeyDao.getUserDefaultEncryptedKey(owner);
            salt = owner;
        } else {
            encryptedKey = encryptedKeyDao.getEncryptedKey(owner, key.id());
            salt = key.salt();
        }

        if (encryptedKey != null && !encryptedKey.key().isBlank()) {
            String keyByteString = encryptionService.decrypt(
                    encryptedKey.key(),
                    encryptionService.getKeyFromPassword(keyPassword, salt, encryptedKey.encryptionAlgorithm()),
                    encryptedKey.iv(),
                    encryptedKey.encryptionAlgorithm());
            return Base64.decode(keyByteString);
        }

        byte[] newKeyToEncrypt = generateRandomKey();
        String iv = SaltGenerator.generateRandomAlphaNumeric(IV_LENGTH);
        String encryptedUserKey = encryptionService.encrypt(
                Base64.encode(newKeyToEncrypt),
                encryptionService.getKeyFromPassword(keyPassword, salt, encryptionService.getCurrentEncryptAlgorithm()),
                iv);

        EncryptedKey newEncryptedKey = new EncryptedKey(encryptedUserKey, encryptionService.getCurrentEncryptAlgorithm(), iv);
        if (key == null) {
            encryptedKeyDao.saveUserDefaultEncryptedKey(owner, newEncryptedKey);
        } else {
            encryptedKeyDao.saveEncryptedKey(owner, key.id(), newEncryptedKey);
        }

        return newKeyToEncrypt;
    }

    private byte[] generateRandomKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE);
            SecretKey secretKey = keyGen.generateKey();

            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            String errorMsg = "Unable to generate user key";

            log.error(errorMsg, ex);
            throw new KeyException(errorMsg, ex);
        }
    }
}