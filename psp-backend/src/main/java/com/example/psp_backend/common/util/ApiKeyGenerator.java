package com.example.psp_backend.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class ApiKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private ApiKeyGenerator() {}

    // nasumičan ključ od 32 bajta, URL-safe Base64
    public static String generateApiKey() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // npr. MRC-8f3a1c92d4e7a1b2
    public static String generateMerchantId() {
        byte[] bytes = new byte[8];
        RANDOM.nextBytes(bytes);
        return "MRC-" + HexFormat.of().formatHex(bytes);
    }

    // SHA-256 u heksadecimalnom zapisu (64 karaktera)
    public static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nije dostupan", e);
        }
    }
}