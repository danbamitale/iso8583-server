package com.titp.server.utils;

import java.security.SecureRandom;

public class RandomUtils {
    private static final SecureRandom random = new SecureRandom();
    private static final String DIGITS = "0123456789";

    public static String getRandomString(int length) {
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return sb.toString();
    }
}
