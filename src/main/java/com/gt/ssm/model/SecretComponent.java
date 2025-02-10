package com.gt.ssm.model;

public record SecretComponent(String id, String secretId, String componentType, String value, boolean encrypted, String encryptionAlgorithm) { }
