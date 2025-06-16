package com.gt.ssm.crypt;

import com.gt.ssm.exception.SecretEncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

@Component
public class AESEncryptionService implements EncryptionServiceForAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(AESEncryptionService.class);

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_GENERATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String KEYSPEC_ALGORITHM = "AES";
    private static final String CHARSET_NAME = "UTF-8";
    private static final int IV_LENGTH = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 128;

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public byte[] getKeyFromPassword(String password, String salt) {
        try{
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_GENERATION_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                    .getEncoded(), ALGORITHM);
            return secret.getEncoded();
        } catch (GeneralSecurityException ex) {
            String errMsg = "Failed to generate key";

            log.error(errMsg, ex);
            throw new SecretEncryptionException(errMsg, ex);
        }
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] keyBytes, String iv) {
        try {
            SecretKey key = new SecretKeySpec(keyBytes, KEYSPEC_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, generateIvSpec(iv));
            byte[] cipherText = cipher.doFinal(data);

            return cipherText;
        } catch (GeneralSecurityException ex) {
            log.error("Exception occurred while encrypting data", ex);
            throw new SecretEncryptionException("Exception occurred while encrypting data", ex);
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted, byte[] keyBytes, String iv) {
        try {
            SecretKey key = new SecretKeySpec(keyBytes, KEYSPEC_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, generateIvSpec(iv));
            byte[] plainText = cipher.doFinal(encrypted);

            return plainText;
        } catch (GeneralSecurityException ex) {
            String errMsg = "Failed to decrypt";

            log.error(errMsg, ex);
            throw new SecretEncryptionException(errMsg, ex);
        }
    }

    private static IvParameterSpec generateIvSpec(String iv) {
        byte[] ivBytes = new byte[IV_LENGTH];
        byte[] ivStringBytes = iv.getBytes(Charset.forName(CHARSET_NAME));

        for (int i = 0; i < Math.min(ivStringBytes.length, IV_LENGTH); i++) {
            ivBytes[i] = ivStringBytes[i];
        }

        return new IvParameterSpec(ivBytes);
    }
}
