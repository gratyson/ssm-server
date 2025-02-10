package com.gt.ssm.secret.model;

public record QlCreditCardComponents(QlSecretComponent companyName,
                                     QlSecretComponent cardNumber,
                                     QlSecretComponent expirationMonth,
                                     QlSecretComponent expirationYear,
                                     QlSecretComponent securityCode) { }