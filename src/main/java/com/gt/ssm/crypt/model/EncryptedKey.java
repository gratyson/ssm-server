package com.gt.ssm.crypt.model;

public record EncryptedKey(String key, String encryptionAlgorithm, String iv) { }
