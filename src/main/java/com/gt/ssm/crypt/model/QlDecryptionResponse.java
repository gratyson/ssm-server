package com.gt.ssm.crypt.model;

import java.util.List;

public record QlDecryptionResponse(boolean success, String errorMsg, List<String> decrypted) { }
