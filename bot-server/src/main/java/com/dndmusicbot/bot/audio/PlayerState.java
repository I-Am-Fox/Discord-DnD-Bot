package com.dndmusicbot.bot.audio;

import com.dndmusicbot.shared.domain.PlaybackStatus;

import java.time.Instant;

public record PlayerState(
    String guildId,
    TrackRef activeTrack,
    int volume,
    PlaybackStatus status,
    Instant updatedAt
) {
    public static PlayerState stopped(String guildId) {
        return new PlayerState(guildId, null, 70, PlaybackStatus.STOPPED, Instant.now());
    }
}
