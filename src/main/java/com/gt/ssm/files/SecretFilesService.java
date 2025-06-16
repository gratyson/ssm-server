package com.gt.ssm.files;

import com.gt.ssm.crypt.EncryptedKeyService;
import com.gt.ssm.crypt.EncryptionService;
import com.gt.ssm.exception.DataIntegrityException;
import com.gt.ssm.exception.SecretEncryptionException;
import com.gt.ssm.exception.SecretFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@Component
public class SecretFilesService {

    private static final Logger log = LoggerFactory.getLogger(SecretFilesService.class);

    private static final String MD5 = "MD5";
    private static final int MD5_SIGNUM = 1;
    private static final int MD5_RADIX = 16;

    private final SecretFilesDao secretFilesDao;
    private final EncryptionService encryptionService;
    private final EncryptedKeyService encryptedKeyService;
    private final int maxFileSize;

    @Autowired
    public SecretFilesService(SecretFilesDao secretFilesDao,
                              EncryptionService encryptionService,
                              EncryptedKeyService encryptedKeyService,
                              @Value("${ssm.secretFile.maxSize}") int maxFileSize) {
        this.secretFilesDao = secretFilesDao;
        this.encryptionService = encryptionService;
        this.encryptedKeyService = encryptedKeyService;
        this.maxFileSize = maxFileSize;
    }

    public String saveSecretFile(String owner, String fileName, String keyId, String keyPassword, byte[] fileBytes) {
        if (fileBytes.length > maxFileSize) {
            throw new SecretFileException("Request file size exceeds max allowed size");
        }

        byte[] key = encryptedKeyService.getKey(owner, keyId, keyPassword);
        String secretFileId = UUID.randomUUID().toString();

        String md5Hash = computeMd5(fileBytes);
        byte[] encryptedFileBytes = encryptionService.encrypt(fileBytes, key, secretFileId);

        secretFilesDao.saveSecretFile(secretFileId, fileName, owner, encryptedFileBytes, md5Hash, encryptionService.getCurrentEncryptAlgorithm());

        return secretFileId;
    }

    public byte[] loadSecretFile(String owner, String fileId, String keyId, String keyPassword) {
        byte[] key = encryptedKeyService.getKey(owner, keyId, keyPassword);
        SecretFilesDao.EncryptedFileData encryptedFileData = secretFilesDao.loadSecretFile(fileId);

        byte[] decryptedBytes = encryptionService.decrypt(encryptedFileData.bytes(), key, fileId, encryptedFileData.encryptionAlogrithm());

        if (encryptedFileData.md5Hash() != null && encryptedFileData.md5Hash() != ""
                && !encryptedFileData.md5Hash().equals(computeMd5(decryptedBytes))) {
            String errorMsg = "Computed and stored hashes do not match";

            log.error(errorMsg);
            throw new DataIntegrityException(errorMsg);
        }

        return decryptedBytes;
    }

    public void deleteSecretFile(String owner, String fileId) {
        secretFilesDao.deleteSecretFile(owner, fileId);
    }

    private String computeMd5(byte[] unencryptedBytes) {
        try {
            byte[] hash = MessageDigest.getInstance(MD5).digest(unencryptedBytes);
            return new BigInteger(MD5_SIGNUM, hash).toString(MD5_RADIX);
        } catch (NoSuchAlgorithmException ex) {
            String errorMsg = "Unable to find " + MD5 + " hash alogrithm.";

            log.error(errorMsg, ex);
            throw new SecretEncryptionException(errorMsg, ex);
        }
    }
}
