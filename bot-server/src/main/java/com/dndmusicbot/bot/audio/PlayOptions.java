package com.dndmusicbot.bot.audio;

import com.dndmusicbot.shared.domain.TransitionMode;

public record PlayOptions(
    int volume,
    boolean loop,
    TransitionMode transitionMode
) {
}
