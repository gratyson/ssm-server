package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponent;
import com.gt.ssm.model.SecretComponentType;

import java.util.List;

public class QlWebsitePasswordComponents extends QlSecretComponents {

    private final QlSecretComponent website;
    private final QlSecretComponent username;
    private final QlSecretComponent password;

    public QlWebsitePasswordComponents(QlSecretComponent website,
                                       QlSecretComponent username,
                                       QlSecretComponent password) {
        this.website = website;
        this.username = username;
        this.password = password;
    }

    public static QlWebsitePasswordComponents fromSecretComponents(List<SecretComponent> secretComponents) {
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

    public QlSecretComponent getWebsite() {
        return website;
    }

    public QlSecretComponent getUsername() {
        return username;
    }

    public QlSecretComponent getPassword() {
        return password;
    }
}