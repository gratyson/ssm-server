package com.gt.ssm.secret.model;

public record QlSecretComponent(String id, String value, boolean encrypted, String encryptionAlgorithm) {
}
