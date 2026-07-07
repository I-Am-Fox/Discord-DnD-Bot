package com.dndmusicbot.bot.errors;

public class ConfigurationException extends MusicBotException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
