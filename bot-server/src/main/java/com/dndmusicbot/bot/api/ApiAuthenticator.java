package com.dndmusicbot.bot.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ApiAuthenticator {
    private final String expectedToken;

    public ApiAuthenticator(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    public boolean isAuthorized(String authorizationHeader, String apiTokenHeader) {
        String providedToken = tokenFromHeaders(authorizationHeader, apiTokenHeader);
        if (providedToken == null) {
            return false;
        }
        byte[] expected = expectedToken.getBytes(StandardCharsets.UTF_8);
        byte[] provided = providedToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, provided);
    }

    private static String tokenFromHeaders(String authorizationHeader, String apiTokenHeader) {
        if (authorizationHeader != null && authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authorizationHeader.substring(7).trim();
            return token.isBlank() ? null : token;
        }
        if (apiTokenHeader != null && !apiTokenHeader.isBlank()) {
            return apiTokenHeader.trim();
        }
        return null;
    }
}
