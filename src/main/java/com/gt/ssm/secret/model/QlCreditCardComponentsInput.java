package com.gt.ssm.secret.model;

import java.util.Map;

public record QlCreditCardComponentsInput(QlComponentInput companyName, QlComponentInput cardNumber, QlComponentInput expirationMonth, QlComponentInput expirationYear, QlComponentInput securityCode) implements QlComponentsInput {
    @Override
    public Map<String, QlComponentInput> toComponentTypeInputs() {
        return new IgnoreNullsMapBuilder()
                .withEntry("company_name", companyName)
                .withEntry("card_number", cardNumber)
                .withEntry("expiration_month", expirationMonth)
                .withEntry("expiration_year", expirationYear)
                .withEntry("security_code", securityCode)
                .Build();
    }
}