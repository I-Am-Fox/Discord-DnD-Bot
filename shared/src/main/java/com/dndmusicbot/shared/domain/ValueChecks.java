package com.dndmusicbot.shared.domain;

public final class ValueChecks {
    private ValueChecks() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    public static int requireVolume(int volume) {
        if (volume < 0 || volume > 150) {
            throw new IllegalArgumentException("volume must be between 0 and 150");
        }
        return volume;
    }
}
