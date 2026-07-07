package com.dndmusicbot.bot.errors;

public class MusicBotException extends RuntimeException {
    public MusicBotException(String message) {
        super(message);
    }

    public MusicBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
