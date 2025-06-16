package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponentType;
import graphql.com.google.common.collect.ImmutableMap;

public record QlCreditCardComponentsInput(QlComponentInput companyName, QlComponentInput cardNumber, QlComponentInput expirationMonth, QlComponentInput expirationYear, QlComponentInput securityCode) implements QlComponentsInput {
    @Override
    public QlComponentInputs toComponentTypeInputs() {
        return new QlComponentInputs(
                new IgnoreNullsMapBuilder()
                    .withEntry(SecretComponentType.COMPANY_NAME, companyName)
                    .withEntry(SecretComponentType.CARD_NUMBER, cardNumber)
                    .withEntry(SecretComponentType.EXPIRATION_MONTH, expirationMonth)
                    .withEntry(SecretComponentType.EXPIRATION_YEAR, expirationYear)
                    .withEntry(SecretComponentType.SECURITY_CODE, securityCode)
                    .Build(),
                ImmutableMap.of());
    }
}