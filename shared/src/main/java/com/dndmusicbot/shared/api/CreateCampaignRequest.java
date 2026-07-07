package com.dndmusicbot.shared.api;

public record CreateCampaignRequest(
    String name,
    String guildId,
    String defaultVoiceChannelId
) {
}
