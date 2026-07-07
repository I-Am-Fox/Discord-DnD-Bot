package com.dndmusicbot.shared.api;

import com.dndmusicbot.shared.domain.SourceType;

import java.time.Duration;
import java.util.List;

public record TrackDto(
    String id,
    String title,
    String source,
    SourceType sourceType,
    Duration duration,
    List<String> tags,
    int defaultVolume
) {
}
