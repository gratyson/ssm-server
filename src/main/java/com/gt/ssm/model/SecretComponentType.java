package com.gt.ssm.model;

public record SecretComponentType(String id, String name, boolean encrypted, String qlName) {

    public static final String WEBSITE = "website";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String COMPANY_NAME = "company_name";
    public static final String CARD_NUMBER = "card_number";
    public static final String SECURITY_CODE = "security_code";
    public static final String EXPIRATION_MONTH = "expiration_month";
    public static final String EXPIRATION_YEAR = "expiration_year";

    public static final String TEXT_BLOB = "text_blob";
}
