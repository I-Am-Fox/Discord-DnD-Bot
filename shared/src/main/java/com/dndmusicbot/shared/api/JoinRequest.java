package com.dndmusicbot.shared.api;

public record JoinRequest(
    String guildId,
    String voiceChannelId
) {
}
