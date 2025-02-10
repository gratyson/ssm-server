package com.gt.ssm.key;

import com.gt.ssm.crypt.EncryptionService;
import com.gt.ssm.crypt.SaltGenerator;
import com.gt.ssm.exception.KeyException;
import com.gt.ssm.key.model.QlKey;
import com.gt.ssm.key.model.QlKeyInput;
import com.gt.ssm.model.Key;
import com.gt.ssm.secret.SecretDao;
import com.gt.ssm.secret.model.QlSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KeyService {

    private static final Logger log = LoggerFactory.getLogger(KeyService.class);

    public static final String USER_PASSWORD_KEY_ID = "0";

    private static final int SALT_LENGTH = 16;

    private final KeyDao keyDao;
    private final KeyTypeService keyTypeService;
    private final SecretDao secretDao;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    public KeyService(KeyDao keyDao, KeyTypeService keyTypeService, SecretDao secretDao, PasswordEncoder passwordEncoder, EncryptionService encryptionService) {
        this.keyDao = keyDao;
        this.keyTypeService = keyTypeService;
        this.secretDao = secretDao;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
    }

    public QlKey getKey(String id, String owner) {
        return keyToQlKey(keyDao.loadKey(id, owner));
    }

    public List<QlKey> getAllOwnedKeys(String owner) {
        return keyDao.loadOwnedKeys(owner).stream().map(key -> keyToQlKey(key)).toList();
    }

    public QlKey saveKey(String owner, QlKeyInput keyInput) {
        validateNewKey(keyInput);

        Key newKey = qlKeyInputToKey(UUID.randomUUID().toString(), owner, keyInput);
        if (keyDao.saveKey(newKey) > 0) {
            return keyToQlKey(newKey);
        }

        throw new KeyException("Unable to save key");
    }

    public QlKey updateKey(String owner, QlKeyInput keyInput) {
        validateUpdateKey(keyInput);

        if (keyDao.updateKey(keyInput.id(), owner, keyInput.toMap()) > 0) {
            return keyToQlKey(keyDao.loadKey(keyInput.id(), owner));
        }

        throw new KeyException("Unable to update key");
    }

    public void deleteKey(String id, String owner) {
        validateNoKeyUsage(id, owner);

        keyDao.deleteKey(id, owner);
    }

    private void validateNewKey(QlKeyInput keyInput) {
        if (keyInput.id() != null && !keyInput.id().isEmpty()) {
            throw new KeyException("New key cannot have existing ID");
        }

        if (keyInput.keyPassword() == null || keyInput.keyPassword().isBlank()) {
            throw new KeyException("New key is missing key password");
        }
    }

    private void validateUpdateKey(QlKeyInput keyInput) {
        if (keyInput.id() == null && keyInput.id().isBlank()) {
            throw new KeyException("Key to update is missing ID");
        }
    }

    private void validateNoKeyUsage(String id, String owner) {
        if (secretDao.isKeyInUse(id)) {
            throw new KeyException("Key is in use.");
        }
    }

    private Key qlKeyInputToKey(String id, String owner, QlKeyInput keyInput) {
        return new Key(
                id,
                owner,
                keyInput.name(),
                keyInput.comments(),
                keyInput.typeId(),
                passwordEncoder.encode(keyInput.keyPassword()),
                SaltGenerator.generateRandomAlphaNumeric(SALT_LENGTH),
                encryptionService.getCurrentEncryptAlgorithm(),
                keyInput.imageName());
    }

    private QlKey keyToQlKey(Key key) {
        if (key == null) {
            return null;
        }

        return new QlKey(
                key.id(),
                key.name(),
                key.comments(),
                keyTypeService.getKeyTypeById(key.typeId()),
                key.salt(),
                key.algorithm(),
                key.imageName());
    }
}
