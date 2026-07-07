package com.dndmusicbot.shared.api;

public record CampaignDto(
    String id,
    String name,
    String guildId,
    String defaultVoiceChannelId
) {
}
