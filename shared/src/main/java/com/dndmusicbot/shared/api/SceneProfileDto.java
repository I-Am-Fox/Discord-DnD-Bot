package com.dndmusicbot.shared.api;

import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;

public record SceneProfileDto(
    String id,
    String campaignId,
    String name,
    SceneType type,
    int defaultVolume,
    TransitionMode transitionIn,
    TransitionMode transitionOut,
    LoopMode loopMode,
    String playlistId
) {
}
