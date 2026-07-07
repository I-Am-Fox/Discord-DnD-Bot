package com.dndmusicbot.bot.audio;

import java.util.concurrent.CompletableFuture;

public interface AudioEngine {
    CompletableFuture<Void> connect(String guildId, String voiceChannelId);

    CompletableFuture<Void> disconnect(String guildId);

    CompletableFuture<TrackRef> resolve(String queryOrUrl);

    CompletableFuture<Void> play(String guildId, TrackRef track, PlayOptions options);

    CompletableFuture<Void> stop(String guildId, FadeOptions fadeOptions);

    CompletableFuture<Void> pause(String guildId);

    CompletableFuture<Void> resume(String guildId);

    CompletableFuture<Void> setVolume(String guildId, int volume);

    CompletableFuture<PlayerState> getState(String guildId);
}
