package com.dndmusicbot.shared.api;

import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;

public record CreateSceneRequest(
    String campaignId,
    String name,
    SceneType type,
    Integer defaultVolume,
    TransitionMode transitionIn,
    TransitionMode transitionOut,
    LoopMode loopMode,
    String playlistId
) {
}
