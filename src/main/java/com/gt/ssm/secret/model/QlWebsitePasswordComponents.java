package com.gt.ssm.secret.model;

public record QlWebsitePasswordComponents(QlSecretComponent website,
                                          QlSecretComponent username,
                                          QlSecretComponent password) { }