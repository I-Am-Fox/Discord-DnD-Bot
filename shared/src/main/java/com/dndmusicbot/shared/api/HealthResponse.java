package com.dndmusicbot.shared.api;

import java.time.Instant;

public record HealthResponse(
    String status,
    String service,
    Instant checkedAt
) {
}
