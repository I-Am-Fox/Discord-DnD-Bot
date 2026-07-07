package com.dndmusicbot.shared.api;

public record PlayRequest(
    String guildId,
    String queryOrUrl
) {
}
