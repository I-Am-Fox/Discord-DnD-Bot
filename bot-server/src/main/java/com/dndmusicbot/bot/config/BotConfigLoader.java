package com.dndmusicbot.bot.config;

import com.dndmusicbot.bot.errors.ConfigurationException;

import java.nio.file.Path;
import java.util.Map;

public final class BotConfigLoader {
    private BotConfigLoader() {
    }

    public static BotConfig loadFromEnvironment() {
        return load(System.getenv());
    }

    public static BotConfig load(Map<String, String> env) {
        String apiToken = required(env, "BOT_API_TOKEN");
        String apiHost = valueOrDefault(env, "BOT_API_HOST", "127.0.0.1");
        int apiPort = parsePort(valueOrDefault(env, "BOT_API_PORT", "8080"), "BOT_API_PORT");
        String dataDirValue = valueOrDefault(env, "DATA_DIR", "./data");
        String logLevel = valueOrDefault(env, "LOG_LEVEL", "INFO");
        int lavalinkPort = parsePort(valueOrDefault(env, "LAVALINK_PORT", "2333"), "LAVALINK_PORT");

        return new BotConfig(
            blankToNull(env.get("DISCORD_TOKEN")),
            apiToken,
            apiHost,
            apiPort,
            blankToNull(env.get("LAVALINK_HOST")),
            lavalinkPort,
            blankToNull(env.get("LAVALINK_PASSWORD")),
            Path.of(dataDirValue).toAbsolutePath().normalize(),
            logLevel
        );
    }

    private static String required(Map<String, String> env, String name) {
        String value = env.get(name);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(name + " is required");
        }
        return value.trim();
    }

    private static String valueOrDefault(Map<String, String> env, String name, String defaultValue) {
        String value = env.get(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int parsePort(String value, String name) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65_535) {
                throw new ConfigurationException(name + " must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new ConfigurationException(name + " must be a valid port", ex);
        }
    }
}
