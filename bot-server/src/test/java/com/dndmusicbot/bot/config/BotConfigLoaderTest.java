package com.dndmusicbot.bot.config;

import com.dndmusicbot.bot.errors.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotConfigLoaderTest {
    @Test
    void loadsDefaultsWithRequiredApiToken() {
        BotConfig config = BotConfigLoader.load(Map.of("BOT_API_TOKEN", "test-token"));

        assertEquals("127.0.0.1", config.apiHost());
        assertEquals(8080, config.apiPort());
        assertEquals(2333, config.lavalinkPort());
        assertEquals("INFO", config.logLevel());
    }

    @Test
    void rejectsMissingApiToken() {
        assertThrows(ConfigurationException.class, () -> BotConfigLoader.load(Map.of()));
    }

    @Test
    void rejectsInvalidPort() {
        Map<String, String> env = Map.of(
            "BOT_API_TOKEN", "test-token",
            "BOT_API_PORT", "99999"
        );

        assertThrows(ConfigurationException.class, () -> BotConfigLoader.load(env));
    }
}
