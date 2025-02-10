package com.gt.ssm.crypt.model;

import java.util.List;

public record QlDecryptionRequest(String key, boolean isUserPassword, String encryptionAlogrithm, List<String> encrypted) { }
