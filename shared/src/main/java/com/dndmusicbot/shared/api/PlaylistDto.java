package com.dndmusicbot.shared.api;

import java.util.List;

public record PlaylistDto(
    String id,
    String campaignId,
    String name,
    List<TrackDto> tracks
) {
}
