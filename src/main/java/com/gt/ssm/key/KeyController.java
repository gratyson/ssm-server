package com.gt.ssm.key;

import com.gt.ssm.exception.KeyException;
import com.gt.ssm.key.model.QlKey;
import com.gt.ssm.key.model.QlKeyInput;
import graphql.schema.DataFetchingFieldSelectionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class KeyController {

    private static final Logger log = LoggerFactory.getLogger(KeyController.class);

    private final KeyService keyService;

    public KeyController(KeyService keyService) {
        this.keyService = keyService;
    }

    @QueryMapping
    public QlKey ownedKey(@AuthenticationPrincipal UserDetails userDetails, @Argument String id) {
        return keyService.getKey(id, userDetails.getUsername());
    }

    @QueryMapping
    public List<QlKey> ownedKeys(@AuthenticationPrincipal UserDetails userDetails) {
        return keyService.getAllOwnedKeys(userDetails.getUsername());
    }

    @MutationMapping
    public SaveKeyResponse saveNewKey(@AuthenticationPrincipal UserDetails userDetails, @Argument QlKeyInput saveKeyInput) {
        try {
            QlKey newKey = keyService.saveKey(userDetails.getUsername(), saveKeyInput);
            return new SaveKeyResponse(true, newKey, "");
        } catch (KeyException ex) {
            return new SaveKeyResponse(false, null, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred while saving a new key", ex);
            return new SaveKeyResponse(false, null, "An unexpected error occurred");
        }
    }

    @MutationMapping
    public SaveKeyResponse updateKey(@AuthenticationPrincipal UserDetails userDetails, @Argument QlKeyInput updateKeyInput) {
        try {
            QlKey newKey = keyService.updateKey(userDetails.getUsername(), updateKeyInput);
            return new SaveKeyResponse(true, newKey, "");
        } catch (KeyException ex) {
            return new SaveKeyResponse(false, null, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred while updating a new key", ex);
            return new SaveKeyResponse(false, null, "An unexpected error occurred");
        }
    }

    @MutationMapping
    public DeleteKeyResponse deleteKey(@AuthenticationPrincipal UserDetails userDetails, @Argument String keyId) {
        try {
            keyService.deleteKey(keyId, userDetails.getUsername());
            return new DeleteKeyResponse(true, "");
        } catch (KeyException ex) {
            return new DeleteKeyResponse(false, ex.getMessage());
        } catch (Exception ex) {
            log.error("An unexpected error occurred while updating a new key", ex);
            return new DeleteKeyResponse(false, "An unexpected error occurred");
        }
    }

    private record SaveKeyResponse(boolean success, QlKey key, String errorMsg) { }
    private record DeleteKeyResponse(boolean success, String errorMsg) { }
}
