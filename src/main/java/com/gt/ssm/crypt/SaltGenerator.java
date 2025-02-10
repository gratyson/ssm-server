package com.gt.ssm.crypt;

import java.util.Random;

public class SaltGenerator {

    private static final String ALPHANUMERIC_CHARSET = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJHKLZXCVBNM0123456789";

    public static String generateRandomAlphaNumeric(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC_CHARSET.charAt(random.nextInt(ALPHANUMERIC_CHARSET.length())));
        }

        return sb.toString();
    }
}
