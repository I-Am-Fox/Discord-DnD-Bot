package com.dndmusicbot.shared.api;

import com.dndmusicbot.shared.domain.TransitionMode;

public record SceneSwitchRequest(
    String guildId,
    String campaignId,
    String sceneName,
    TransitionMode transitionMode
) {
}
