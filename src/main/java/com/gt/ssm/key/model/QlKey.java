package com.gt.ssm.key.model;

import com.gt.ssm.model.KeyType;

public record QlKey(String id, String name, String comments, KeyType type, String salt, String algorithm, String imageName) { }