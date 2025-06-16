package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponent;
import com.gt.ssm.model.SecretComponentType;

import java.util.Enumeration;
import java.util.List;

public class QlTextBlobComponents extends QlSecretComponents {

    private final QlSecretComponent textBlob;

    public QlTextBlobComponents(QlSecretComponent textBlob) {
        this.textBlob = textBlob;
    }

    public static QlTextBlobComponents fromSecretComponents(Iterable<SecretComponent> secretComponents) {
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

    public QlSecretComponent getTextBlob() {
        return textBlob;
    }
}