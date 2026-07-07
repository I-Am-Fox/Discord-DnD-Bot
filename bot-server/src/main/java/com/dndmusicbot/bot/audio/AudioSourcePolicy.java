package com.dndmusicbot.bot.audio;

import com.dndmusicbot.shared.domain.ValueChecks;

import java.net.URI;
import java.util.Locale;

public class AudioSourcePolicy {
    public String normalizePlayableQuery(String queryOrUrl) {
        String value = ValueChecks.requireText(queryOrUrl, "queryOrUrl");
        if (isUrl(value) || hasKnownSourcePrefix(value)) {
            return value;
        }
        return "ytsearch:" + value;
    }

    private static boolean isUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean hasKnownSourcePrefix(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.startsWith("ytsearch:")
            || normalized.startsWith("ytmsearch:")
            || normalized.startsWith("scsearch:");
    }
}
