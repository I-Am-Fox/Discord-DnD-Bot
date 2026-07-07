package com.dndmusicbot.shared.api;

public record VolumeRequest(
    String guildId,
    int volume
) {
}
