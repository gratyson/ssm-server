package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponent;

public abstract class QlSecretComponents {

    protected static QlSecretComponent toQlSecretComponent(SecretComponent secretComponent) {
        if (secretComponent == null) {
            return null;
        }

        return new QlSecretComponent(secretComponent.id(), secretComponent.value(), secretComponent.encrypted(), secretComponent.encryptionAlgorithm());
    }
}
