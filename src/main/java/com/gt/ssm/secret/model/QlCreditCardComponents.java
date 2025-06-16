package com.gt.ssm.secret.model;

import com.gt.ssm.model.SecretComponent;
import com.gt.ssm.model.SecretComponentType;

import java.util.List;

public class QlCreditCardComponents extends QlSecretComponents {

    private final QlSecretComponent companyName;
    private final QlSecretComponent cardNumber;
    private final QlSecretComponent expirationMonth;
    private final QlSecretComponent expirationYear;
    private final QlSecretComponent securityCode;

    public QlCreditCardComponents(QlSecretComponent companyName,
                                  QlSecretComponent cardNumber,
                                  QlSecretComponent expirationMonth,
                                  QlSecretComponent expirationYear,
                                  QlSecretComponent securityCode) {
        this.companyName = companyName;
        this.cardNumber = cardNumber;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.securityCode = securityCode;
    }

    public static QlCreditCardComponents fromSecretComponents(List<SecretComponent> secretComponents) {
        SecretComponent companyName = null;
        SecretComponent cardNumberComponent = null;
        SecretComponent securityCodeComponent = null;
        SecretComponent expirationMonthComponent = null;
        SecretComponent expirationYearComponent = null;

        for(SecretComponent secretComponent : secretComponents) {
            switch(secretComponent.componentType()) {
                case SecretComponentType.COMPANY_NAME:
                    companyName = secretComponent;
                    break;
                case SecretComponentType.CARD_NUMBER:
                    cardNumberComponent = secretComponent;
                    break;
                case SecretComponentType.SECURITY_CODE:
                    securityCodeComponent = secretComponent;
                    break;
                case SecretComponentType.EXPIRATION_MONTH:
                    expirationMonthComponent = secretComponent;
                    break;
                case SecretComponentType.EXPIRATION_YEAR:
                    expirationYearComponent = secretComponent;
                    break;
            }
        }

        if (companyName != null ||
                cardNumberComponent != null ||
                securityCodeComponent != null ||
                expirationMonthComponent != null ||
                expirationYearComponent != null) {
            return new QlCreditCardComponents(
                    toQlSecretComponent(companyName),
                    toQlSecretComponent(cardNumberComponent),
                    toQlSecretComponent(expirationMonthComponent),
                    toQlSecretComponent(expirationYearComponent),
                    toQlSecretComponent(securityCodeComponent));
        }

        return null;
    }

    public QlSecretComponent getCompanyName() {
        return companyName;
    }

    public QlSecretComponent getCardNumber() {
        return cardNumber;
    }

    public QlSecretComponent getExpirationMonth() {
        return expirationMonth;
    }

    public QlSecretComponent getExpirationYear() {
        return expirationYear;
    }

    public QlSecretComponent getSecurityCode() {
        return securityCode;
    }
}