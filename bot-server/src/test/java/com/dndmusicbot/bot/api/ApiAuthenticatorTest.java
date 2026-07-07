package com.dndmusicbot.bot.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthenticatorTest {
    @Test
    void acceptsBearerToken() {
        ApiAuthenticator authenticator = new ApiAuthenticator("secret-token");

        assertTrue(authenticator.isAuthorized("Bearer secret-token", null));
    }

    @Test
    void acceptsApiTokenHeader() {
        ApiAuthenticator authenticator = new ApiAuthenticator("secret-token");

        assertTrue(authenticator.isAuthorized(null, "secret-token"));
    }

    @Test
    void rejectsMissingOrWrongToken() {
        ApiAuthenticator authenticator = new ApiAuthenticator("secret-token");

        assertFalse(authenticator.isAuthorized(null, null));
        assertFalse(authenticator.isAuthorized("Bearer wrong", null));
    }
}
