package com.gt.ssm.secret;

import com.gt.ssm.crypt.EncryptionService;
import com.gt.ssm.exception.DaoException;
import com.gt.ssm.key.KeyService;
import com.gt.ssm.key.model.QlKey;
import com.gt.ssm.crypt.EncryptedKeyService;
import com.gt.ssm.model.Secret;
import com.gt.ssm.model.SecretComponent;
import com.gt.ssm.model.SecretComponentType;
import com.gt.ssm.secret.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SecretService {

    private static final Logger log = LoggerFactory.getLogger(SecretService.class);

    private final SecretDao secretDao;
    private final SecretTypeService secretTypeService;
    private final KeyService keyService;
    private final SecretComponentTypeService secretComponentTypeService;
    private final EncryptionService encryptionService;
    private final EncryptedKeyService encryptedKeyService;

    @Autowired
    public SecretService(SecretDao secretDao,
                         SecretTypeService secretTypeService,
                         KeyService keyService,
                         SecretComponentTypeService secretComponentTypeService,
                         EncryptionService encryptionService,
                         EncryptedKeyService encryptedKeyService) {
        this.secretDao = secretDao;
        this.secretTypeService = secretTypeService;
        this.keyService = keyService;
        this.secretComponentTypeService = secretComponentTypeService;
        this.encryptionService = encryptionService;
        this.encryptedKeyService = encryptedKeyService;
    }

    public List<QlSecret> getOwnedSecrets(String owner, Collection<String> components, boolean keyRequested) {
        List<Secret> ownedSecrets = secretDao.getSecretsWithComponents(owner, qlComponentNamesToComponentIds(components));

        return ownedSecrets.stream().map(secret -> toQlSecret(secret, keyRequested)).toList();
    }

    public QlSecret getOwnedSecret(String id, String owner, Collection<String> components, boolean keyRequested) {
        Secret secret = secretDao.getSecretWithComponents(id, owner, qlComponentNamesToComponentIds(components));

        if (secret == null) {
            return null;
        }

        return toQlSecret(secret, keyRequested);
    }

    public QlSecret saveSecret(QlSecretInput input, String owner, String keyPassword, boolean keyRequested) {
        Secret secretToSave = toSecret(input, owner, keyPassword);

        if (secretDao.saveSecret(secretToSave) > 0) {
            return toQlSecret(secretToSave, keyRequested);
        }

        throw new DaoException("Failed to save secret");
    }

    public void deleteSecret(String id, String owner) {
        verifyOwner(id, owner);

        if (secretDao.deleteSecret(id) == 0) {
            log.warn("User {} was unable to delete secret {}", owner, id);
            throw new DaoException("No owned secret to delete");
        }
    }

    public QlSecret unlockSecret(String secretId, String owner, String keyPassword, Collection<String> components, boolean keyRequested) {
        Secret secretToUnlock = secretDao.getSecretWithComponents(secretId, owner, qlComponentNamesToComponentIds(components));

        Secret unlockedSecret = new Secret(
                secretToUnlock.id(),
                secretToUnlock.owner(),
                secretToUnlock.imageName(),
                secretToUnlock.secretType(),
                secretToUnlock.name(),
                secretToUnlock.comments(),
                secretToUnlock.keyId(),
                unlockComponents(secretToUnlock.keyId(), owner, secretToUnlock.secretComponents(), keyPassword));

        return toQlSecret(unlockedSecret, keyRequested);
    }

    private void verifyOwner(String id, String owner) {
        Secret secret = secretDao.getSecretWithComponents(id, owner, List.of());

        if (secret == null || !secret.owner().equals(owner)) {
            log.warn("User {} attempted to access secret {} without permission", owner, id);
            throw new SecurityException("Access not permitted");
        }
    }

    private List<SecretComponent> unlockComponents(String keyId, String owner, List<SecretComponent> componentsToUnlock, String keyPassword) {
        byte[] key = encryptedKeyService.getKey(owner, keyId, keyPassword);

        List<SecretComponent> unlockedComponents = new ArrayList<>();

        for(SecretComponent secretComponent : componentsToUnlock) {
            if (secretComponent.encrypted()) {
                unlockedComponents.add(new SecretComponent(
                        secretComponent.id(),
                        secretComponent.secretId(),
                        secretComponent.componentType(),
                        encryptionService.decrypt(secretComponent.value(), key, secretComponent.id(), secretComponent.encryptionAlgorithm()),
                        false,
                        ""));
            } else {
                unlockedComponents.add(secretComponent);
            }
        }

        return unlockedComponents;
    }

    private QlSecret toQlSecret(Secret secret, boolean keyRequested) {
        QlKey key;
        if (keyRequested) {
            key = loadKey(secret.owner(), secret.keyId());
        } else {
            key = new QlKey(secret.keyId(), "", "", null, "", "", "");
        }

        return secret == null ? null :
                new QlSecret(
                    secret.id(),
                    secret.imageName(),
                    secret.name(),
                    secret.comments(),
                    secretTypeService.getSecretTypeById(secret.secretType()),
                    key,
                    getWebsitePasswordComponents(secret.secretComponents()),
                    getCreditCardComponents(secret.secretComponents()),
                    getTextBlobComponents(secret.secretComponents()));
    }

    private Secret toSecret(QlSecretInput secretInput, String owner, String keyPassword) {
        String id = secretInput.id();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        // Client won't ask the user for a password if it determines that is unneeded
        byte[] key = keyPassword == null || keyPassword.isBlank() ? new byte[0] : encryptedKeyService.getKey(owner, secretInput.keyId(), keyPassword);

        List<SecretComponent> secretComponents = new ArrayList<>();
        secretComponents.addAll(toSecretComponents(secretInput.websitePasswordComponents(), id, key));
        secretComponents.addAll(toSecretComponents(secretInput.creditCardComponents(), id, key));
        secretComponents.addAll(toSecretComponents(secretInput.textBlobComponents(), id, key));

        return new Secret(id, owner, secretInput.imageName(), secretInput.typeId(), secretInput.name(), secretInput.comments(), secretInput.keyId(), secretComponents);
    }

    private List<SecretComponent> toSecretComponents(QlComponentsInput componentsInput, String secretId, byte[] key) {
        List<SecretComponent> secretComponents = new ArrayList<>();

        if (componentsInput != null) {
            for (Map.Entry<String, QlComponentInput> componentEntry : componentsInput.toComponentTypeInputs().entrySet()) {
                secretComponents.add(toSecretComponent(componentEntry.getKey(), componentEntry.getValue(), secretId, key));
            }
        }

        return secretComponents;
    }

    private QlWebsitePasswordComponents getWebsitePasswordComponents(List<SecretComponent> secretComponents) {
        SecretComponent websiteComponent = null;
        SecretComponent usernameComponent = null;
        SecretComponent passwordComponent = null;

        for(SecretComponent secretComponent : secretComponents) {
            switch(secretComponent.componentType()) {
                case SecretComponentType.WEBSITE:
                    websiteComponent = secretComponent;
                    break;
                case SecretComponentType.USERNAME:
                    usernameComponent = secretComponent;
                    break;
                case SecretComponentType.PASSWORD:
                    passwordComponent = secretComponent;
                    break;
            }
        }

        if (websiteComponent != null || usernameComponent != null || passwordComponent != null) {
            return new QlWebsitePasswordComponents(
                    toQlSecretComponent(websiteComponent),
                    toQlSecretComponent(usernameComponent),
                    toQlSecretComponent(passwordComponent));
        }

        return null;
    }

    private SecretComponent toSecretComponent(String componentTypeId, QlComponentInput componentInput, String secretId, byte[] key) {
        SecretComponentType secretComponentType = secretComponentTypeService.getComponentByTypeId(componentTypeId);

        String componentIdToUse = componentInput.id();
        if (componentIdToUse == null || componentIdToUse.isEmpty()) {
            componentIdToUse = UUID.randomUUID().toString();
        }

        if (secretComponentType != null && componentInput.value() != null && !componentInput.value().isEmpty() && secretComponentType.encrypted()) {
            String encryptedValue = encryptionService.encrypt(componentInput.value(), key, componentIdToUse);

            return new SecretComponent(componentIdToUse, secretId, componentTypeId, encryptedValue, true, encryptionService.getCurrentEncryptAlgorithm());
        }

        return new SecretComponent(componentIdToUse, secretId, componentTypeId, componentInput.value(), false, "");
    }

    private QlCreditCardComponents getCreditCardComponents(List<SecretComponent> secretComponents) {
        SecretComponent companyName = null;
        SecretComponent cardNumberComponent = null;
        SecretComponent securityCodeComponent = null;
        SecretComponent expirationMonthComponent = null;
        SecretComponent expirationYearComponent = null;

        for(SecretComponent secretComponent : secretComponents) {
            switch(secretComponent.componentType()) {
                case SecretComponentType.COMPANY_NAME:
                    companyName = secretComponent;
                    break;
                case SecretComponentType.CARD_NUMBER:
                    cardNumberComponent = secretComponent;
                    break;
                case SecretComponentType.SECURITY_CODE:
                    securityCodeComponent = secretComponent;
                    break;
                case SecretComponentType.EXPIRATION_MONTH:
                    expirationMonthComponent = secretComponent;
                    break;
                case SecretComponentType.EXPIRATION_YEAR:
                    expirationYearComponent = secretComponent;
                    break;
            }
        }

        if (companyName != null ||
            cardNumberComponent != null ||
            securityCodeComponent != null ||
            expirationMonthComponent != null ||
            expirationYearComponent != null) {
            return new QlCreditCardComponents(
                    toQlSecretComponent(companyName),
                    toQlSecretComponent(cardNumberComponent),
                    toQlSecretComponent(expirationMonthComponent),
                    toQlSecretComponent(expirationYearComponent),
                    toQlSecretComponent(securityCodeComponent));
        }

        return null;
    }

    private QlTextBlobComponents getTextBlobComponents(List<SecretComponent> secretComponents) {
        SecretComponent textBlob = null;

        for(SecretComponent secretComponent : secretComponents) {
            switch (secretComponent.componentType()) {
                case SecretComponentType.TEXT_BLOB:
                    textBlob = secretComponent;
                    break;
            }
        }

        if (textBlob != null) {
            return new QlTextBlobComponents(
                    toQlSecretComponent(textBlob));
        }

        return null;
    }

    private QlSecretComponent toQlSecretComponent(SecretComponent secretComponent) {
        if (secretComponent == null) {
            return null;
        }

        return new QlSecretComponent(secretComponent.id(), secretComponent.value(), secretComponent.encrypted(), secretComponent.encryptionAlgorithm());
    }

    private QlKey loadKey(String owner, String keyId) {
        if (keyId == null || keyId.isBlank() || keyId.equals(KeyService.USER_PASSWORD_KEY_ID)) {
            return new QlKey(KeyService.USER_PASSWORD_KEY_ID, "", "", null, "", "", "");
        }

        return keyService.getKey(keyId, owner);
    }

    private List<String> qlComponentNamesToComponentIds(Collection<String> qlComponentNames) {
        if (qlComponentNames == null) {
            return List.of();
        }

        return qlComponentNames.stream().map(qlComponentName -> secretComponentTypeService.getComponentByTypeQlName(qlComponentName).id()).toList();
    }
}
