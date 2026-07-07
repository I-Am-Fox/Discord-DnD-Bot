package com.dndmusicbot.bot.errors;

public class AudioEngineException extends MusicBotException {
    public AudioEngineException(String message) {
        super(message);
    }

    public AudioEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
