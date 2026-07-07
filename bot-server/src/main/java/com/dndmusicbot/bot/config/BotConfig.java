package com.dndmusicbot.bot.config;

import java.nio.file.Path;

public record BotConfig(
    String discordToken,
    String apiToken,
    String apiHost,
    int apiPort,
    String lavalinkHost,
    int lavalinkPort,
    String lavalinkPassword,
    Path dataDir,
    String logLevel
) {
    public String safeSummary() {
        return "BotConfig[apiHost=%s, apiPort=%d, dataDir=%s, logLevel=%s, discordTokenSet=%s, lavalinkHostSet=%s]"
            .formatted(apiHost, apiPort, dataDir, logLevel, !isBlank(discordToken), !isBlank(lavalinkHost));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
