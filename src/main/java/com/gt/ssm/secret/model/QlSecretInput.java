package com.gt.ssm.secret.model;

public record QlSecretInput(String id,
                            String imageName,
                            String name,
                            String comments,
                            String typeId,
                            String keyId,
                            String keyPassword,
                            QlWebsitePasswordComponentsInput websitePasswordComponents,
                            QlCreditCardComponentsInput creditCardComponents,
                            QlTextBlobComponentsInput textBlobComponents) { }