package com.dndmusicbot.shared.api;

import com.dndmusicbot.shared.domain.PlaybackStatus;
import com.dndmusicbot.shared.domain.TransitionMode;

import java.time.Instant;
import java.util.List;

public record PlaybackStateDto(
    String guildId,
    String campaignId,
    String activeSceneId,
    String activeSceneName,
    String activeTrackId,
    List<String> queue,
    int volume,
    PlaybackStatus status,
    TransitionMode lastTransition,
    Instant updatedAt
) {
}
