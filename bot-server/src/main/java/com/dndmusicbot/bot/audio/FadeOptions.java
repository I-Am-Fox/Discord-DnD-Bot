package com.dndmusicbot.bot.audio;

import java.time.Duration;

public record FadeOptions(Duration duration) {
    public static FadeOptions none() {
        return new FadeOptions(Duration.ZERO);
    }
}
