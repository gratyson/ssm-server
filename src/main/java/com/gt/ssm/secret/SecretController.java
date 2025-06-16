package com.gt.ssm.secret;

import com.gt.ssm.exception.SsmException;
import com.gt.ssm.secret.model.QlSecret;
import com.gt.ssm.secret.model.QlSecretInput;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SecretController {

    private static final Logger log = LoggerFactory.getLogger(SecretController.class);

    private static final List<String> BASE_SECRET_NAME_FIELDS = List.of(
            "secret",
            "secrets"
    );
    private static final List<String> SECRET_COMPONENT_FIELDS = List.of(
            "*Components"
    );
    private static final int SECRET_COMPONENT_FIELD_LEVEL = 4;

    private static final String KEY_FIELD_NAME = "key";
    private static final String KEY_FIELD_ID_NAME = "id";
    private static final int KEY_FIELD_LEVEL = 4;

    private static final String SECRET_COMPONENT_TYPE_NAME = "SecretComponent";

    private final SecretService secretService;
    private final SecretTypeService secretTypeService;

    @Autowired
    public SecretController(SecretService secretService, SecretTypeService secretTypeService) {
        this.secretService = secretService;
        this.secretTypeService = secretTypeService;
    }

    @QueryMapping()
    public QlSecretsResponse ownedSecrets(@AuthenticationPrincipal UserDetails userDetails, DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        try {
            List<QlSecret> secrets = secretService.getOwnedSecrets(userDetails.getUsername(), parseSecretComponents(dataFetchingFieldSelectionSet), hasKeyDataFields(dataFetchingFieldSelectionSet));
            return new QlSecretsResponse(true, secrets, "");
        } catch (SsmException ex) {
            return new QlSecretsResponse(false, List.of(), ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred loading secrets", ex);
            return new QlSecretsResponse(false, List.of(), "An unexpected error occurred");
        }
    }

    @QueryMapping()
    public QlSecretResponse ownedSecret(@AuthenticationPrincipal UserDetails userDetails, @Argument String id, DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        try {
            QlSecret secret = secretService.getOwnedSecret(
                    id,
                    userDetails.getUsername(),
                    parseSecretComponents(dataFetchingFieldSelectionSet),
                    hasKeyDataFields(dataFetchingFieldSelectionSet));
            return new QlSecretResponse(true, secret, "");
        } catch (SsmException ex) {
            return new QlSecretResponse(false, null, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred loading a secret", ex);
            return new QlSecretResponse(false, null, "An unexpected error occurred");
        }
    }

    @MutationMapping
    public QlSecretResponse saveSecret(@AuthenticationPrincipal UserDetails userDetails, @Argument QlSecretInput secretInput, DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        try {
            QlSecret secret = secretService.saveSecret(secretInput, userDetails.getUsername(), secretInput.keyPassword(), hasKeyDataFields(dataFetchingFieldSelectionSet));
            return new QlSecretResponse(true, secret, "");
        } catch (SsmException ex) {
            return new QlSecretResponse(false, null, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred saving a secret", ex);
            return new QlSecretResponse(false, null, "An unexpected error occurred");
        }
    }

    @MutationMapping
    public QlSecretResponse unlockSecret(@AuthenticationPrincipal UserDetails userDetails, @Argument QlUnlockRequest unlockRequest, DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        try {
            QlSecret unlockedSecret = secretService.unlockSecret(
                unlockRequest.secretId(),
                userDetails.getUsername(),
                unlockRequest.keyPassword(),
                parseSecretComponents(dataFetchingFieldSelectionSet),
                hasKeyDataFields(dataFetchingFieldSelectionSet));
            return new QlSecretResponse(true, unlockedSecret, "");
        } catch (SsmException ex) {
            return new QlSecretResponse(false, null, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred loading a secret", ex);
            return new QlSecretResponse(false, null, "An unexpected error occurred");
        }
    }

    @MutationMapping
    public QlDeleteSecretResponse deleteSecret(@AuthenticationPrincipal UserDetails userDetails, @Argument String secretId) {
        try {
            secretService.deleteSecret(secretId, userDetails.getUsername());
            return new QlDeleteSecretResponse(true, "");
        } catch (SsmException ex) {
            return new QlDeleteSecretResponse(false, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred deleting a secret", ex);
            return new QlDeleteSecretResponse(false, "An unexpected error occurred");
        }
    }

    private List<String> parseSecretComponents(DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        List<String> selectedSecretComponents = new ArrayList<>();

        for(String baseSecretFieldName : BASE_SECRET_NAME_FIELDS) {
            for (SelectedField baseSecretField : dataFetchingFieldSelectionSet.getFields(baseSecretFieldName)) {
                for (String componentFieldName : SECRET_COMPONENT_FIELDS) {
                    for (SelectedField secretComponentFields : baseSecretField.getSelectionSet().getFields(componentFieldName)) {
                        for (SelectedField selectedField : secretComponentFields.getSelectionSet().getFields()) {
                            if (selectedField.getLevel() == SECRET_COMPONENT_FIELD_LEVEL && !selectedField.getName().startsWith("_")) {
                                selectedSecretComponents.addAll(processComponentField(selectedField));
                            }
                        }
                    }
                }
            }
        }

        return selectedSecretComponents;
    }

    private List<String> processComponentField(SelectedField selectedField) {
        if (isFieldSecretComponent(selectedField)) {
            return List.of(selectedField.getName());
        }

        List<String> componentFields = new ArrayList<>();
        for(SelectedField childField : selectedField.getSelectionSet().getImmediateFields()) {
            componentFields.addAll(processComponentField(childField));
        }
        return componentFields;
    }

    private boolean isFieldSecretComponent(SelectedField selectedField) {
        List<SelectedField> childFields = selectedField.getSelectionSet().getImmediateFields();

        if (childFields != null && childFields.size() > 0) {
            return childFields.get(0).getObjectTypeNames().contains("SecretComponent");
        }

        return false;
    }

    private boolean hasKeyDataFields(DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet) {
        for(String baseSecretFieldName : BASE_SECRET_NAME_FIELDS) {
            for (SelectedField baseSecretField : dataFetchingFieldSelectionSet.getFields(baseSecretFieldName)) {
                for (SelectedField secretComponentFields : baseSecretField.getSelectionSet().getFields(KEY_FIELD_NAME)) {
                    for (SelectedField selectedField : secretComponentFields.getSelectionSet().getFields()) {
                        if (selectedField.getLevel() == KEY_FIELD_LEVEL && !selectedField.getName().equals(KEY_FIELD_ID_NAME) && !selectedField.getName().startsWith("_")) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private record QlUnlockRequest(String secretId, String keyPassword) { }

    private record QlSecretResponse(boolean success, QlSecret secret, String errorMsg) { }
    private record QlSecretsResponse(boolean success, List<QlSecret> secrets, String errorMsg) { }
    private record QlDeleteSecretResponse(boolean success, String errorMsg) { }
}
