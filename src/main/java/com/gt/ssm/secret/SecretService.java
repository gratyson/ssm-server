package com.gt.ssm.secret;

import com.gt.ssm.crypt.EncryptionService;
import com.gt.ssm.exception.DaoException;
import com.gt.ssm.files.SecretFilesService;
import com.gt.ssm.key.KeyService;
import com.gt.ssm.key.model.QlKey;
import com.gt.ssm.crypt.EncryptedKeyService;
import com.gt.ssm.model.Secret;
import com.gt.ssm.model.SecretComponent;
import com.gt.ssm.model.SecretComponentType;
import com.gt.ssm.model.SecretType;
import com.gt.ssm.secret.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SecretService {

    private static final Logger log = LoggerFactory.getLogger(SecretService.class);

    private final SecretDao secretDao;
    private final SecretTypeService secretTypeService;
    private final SecretFilesService secretFilesService;
    private final KeyService keyService;
    private final SecretComponentTypeService secretComponentTypeService;
    private final EncryptionService encryptionService;
    private final EncryptedKeyService encryptedKeyService;

    @Autowired
    public SecretService(SecretDao secretDao,
                         SecretTypeService secretTypeService,
                         SecretFilesService secretFilesService,
                         KeyService keyService,
                         SecretComponentTypeService secretComponentTypeService,
                         EncryptionService encryptionService,
                         EncryptedKeyService encryptedKeyService) {
        this.secretDao = secretDao;
        this.secretTypeService = secretTypeService;
        this.secretFilesService = secretFilesService;
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

        List<String> filesToDelete = getSecretFilesToDelete(secretToSave, owner);

        if (secretDao.saveSecret(secretToSave) > 0) {
            deleteSecretFiles(filesToDelete, owner);
            return toQlSecret(secretToSave, keyRequested);
        }

        throw new DaoException("Failed to save secret");
    }

    public void deleteSecret(String id, String owner) {
        verifyOwner(id, owner);

        List<String> secretFileIdsToDelete = getSecretFilesToDelete(id, owner);

        if (secretDao.deleteSecret(id) > 0) {
            deleteSecretFiles(secretFileIdsToDelete, owner);
        }
        else {
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

    private void deleteSecretFiles(List<String> fileIds, String owner) {
        for (String fileIdToDelete : fileIds) {
            try {
                secretFilesService.deleteSecretFile(owner, fileIdToDelete);
            } catch (Exception ex) {
                log.warn("Failed to delete file {} after deleting secret", fileIdToDelete);
            }
        }
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
                        secretComponent.line(),
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
                    QlWebsitePasswordComponents.fromSecretComponents(secret.secretComponents()),
                    QlCreditCardComponents.fromSecretComponents(secret.secretComponents()),
                    QlTextBlobComponents.fromSecretComponents(secret.secretComponents()),
                    QlFilesComponents.fromSecretComponents(secret.secretComponents()));
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
        secretComponents.addAll(toSecretComponents(secretInput.filesComponents(), id, key));

        return new Secret(id, owner, secretInput.imageName(), secretInput.typeId(), secretInput.name(), secretInput.comments(), secretInput.keyId(), secretComponents);
    }

    private List<SecretComponent> toSecretComponents(QlComponentsInput componentsInput, String secretId, byte[] key) {
        List<SecretComponent> secretComponents = new ArrayList<>();

        if (componentsInput != null) {
            for (Map.Entry<String, QlComponentInput> componentEntry : componentsInput.toComponentTypeInputs().scalarComponents().entrySet()) {
                secretComponents.add(toSecretComponent(componentEntry.getKey(), componentEntry.getValue(), secretId, key));
            }
            for (Map.Entry<String, QlComponentInput[]> componentEntry : componentsInput.toComponentTypeInputs().arrayComponents().entrySet()) {
                for (int line = 0; line < componentEntry.getValue().length; line++) {
                    secretComponents.add(toSecretComponent(componentEntry.getKey(), componentEntry.getValue()[line], line, secretId, key));
                }
            }
        }

        return secretComponents;
    }

    private SecretComponent toSecretComponent(String componentTypeId, QlComponentInput componentInput, String secretId, byte[] key) {
        return toSecretComponent(componentTypeId, componentInput, 1, secretId, key);
    }

    private SecretComponent toSecretComponent(String componentTypeId, QlComponentInput componentInput, int line, String secretId, byte[] key) {
        SecretComponentType secretComponentType = secretComponentTypeService.getComponentByTypeId(componentTypeId);

        String componentIdToUse = componentInput.id();
        if (componentIdToUse == null || componentIdToUse.isEmpty()) {
            componentIdToUse = UUID.randomUUID().toString();
        }

        if (secretComponentType != null && componentInput.value() != null && !componentInput.value().isEmpty() && secretComponentType.encrypted()) {
            String encryptedValue = encryptionService.encrypt(componentInput.value(), key, componentIdToUse);

            return new SecretComponent(componentIdToUse, line, secretId, componentTypeId, encryptedValue, true, encryptionService.getCurrentEncryptAlgorithm());
        }

        return new SecretComponent(componentIdToUse, line, secretId, componentTypeId, componentInput.value(), false, "");
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

    private List<String> getSecretFilesToDelete(String secretId, String owner) {
        return getSecretFilesToDelete(secretId, owner, Set.of());
    }

    private List<String> getSecretFilesToDelete(Secret secretToSave, String owner) {
        Set<String> fileIdsToSave = secretToSave.secretComponents()
                .stream()
                .filter(secretComponent -> secretComponent.componentType().equals(SecretComponentType.FILE_ID))
                .map(secretComponent -> secretComponent.value())
                .collect(Collectors.toSet());

        if (fileIdsToSave.isEmpty()) {
            return List.of();
        } else {
            return getSecretFilesToDelete(secretToSave.id(), owner, fileIdsToSave);
        }
    }

    private List<String> getSecretFilesToDelete(String secretId, String owner, Set<String> idsToKeep) {
        Secret secret = secretDao.getSecretWithComponents(secretId, owner, List.of(SecretComponentType.FILE_ID));

        if (secret == null || secret.secretComponents() == null) {
            return List.of();
        }

        return secret.secretComponents().stream()
                .filter(secretComponent -> (
                        secretComponent.componentType().equals(SecretComponentType.FILE_ID))
                        && !secretComponent.encrypted()
                        && !idsToKeep.contains(secretComponent.value()))
                .map(secretComponent -> secretComponent.value())
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }
}