package com.dndmusicbot.bot.api;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiRateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public ApiRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean allow(String key) {
        long now = clock.millis();
        Window updated = windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAtMillis() >= windowMillis) {
                return new Window(now, 1);
            }
            return new Window(current.startedAtMillis(), current.count() + 1);
        });
        return updated.count() <= maxRequests;
    }

    private record Window(long startedAtMillis, int count) {
    }
}
