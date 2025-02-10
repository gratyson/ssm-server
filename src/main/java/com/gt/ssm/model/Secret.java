package com.gt.ssm.model;

import java.util.List;

public record Secret(String id, String owner, String imageName, String secretType, String name, String comments, String keyId, List<SecretComponent> secretComponents) { }


