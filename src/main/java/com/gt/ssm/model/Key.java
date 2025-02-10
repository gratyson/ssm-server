package com.gt.ssm.model;

public record Key(String id, String owner, String name, String comments, String typeId, String keyPassword, String salt, String algorithm, String imageName) { }
