package com.dndmusicbot.bot.audio;

import com.dndmusicbot.shared.domain.PlaybackStatus;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NoopAudioEngine implements AudioEngine {
    private final Map<String, PlayerState> states = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> connect(String guildId, String voiceChannelId) {
        states.put(guildId, new PlayerState(guildId, null, currentVolume(guildId), PlaybackStatus.STOPPED, Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> disconnect(String guildId) {
        states.put(guildId, new PlayerState(guildId, null, currentVolume(guildId), PlaybackStatus.DISCONNECTED, Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<TrackRef> resolve(String queryOrUrl) {
        TrackRef track = new TrackRef("resolved-" + Math.abs(queryOrUrl.hashCode()), queryOrUrl, queryOrUrl);
        return CompletableFuture.completedFuture(track);
    }

    @Override
    public CompletableFuture<Void> play(String guildId, TrackRef track, PlayOptions options) {
        states.put(guildId, new PlayerState(guildId, track, options.volume(), PlaybackStatus.PLAYING, Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop(String guildId, FadeOptions fadeOptions) {
        states.put(guildId, new PlayerState(guildId, null, currentVolume(guildId), PlaybackStatus.STOPPED, Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> pause(String guildId) {
        PlayerState current = state(guildId);
        states.put(guildId, new PlayerState(guildId, current.activeTrack(), current.volume(), PlaybackStatus.PAUSED, Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> resume(String guildId) {
        PlayerState current = state(guildId);
        PlaybackStatus status = current.activeTrack() == null ? PlaybackStatus.STOPPED : PlaybackStatus.PLAYING;
        states.put(guildId, new PlayerState(guildId, current.activeTrack(), current.volume(), status, Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setVolume(String guildId, int volume) {
        PlayerState current = state(guildId);
        states.put(guildId, new PlayerState(guildId, current.activeTrack(), volume, current.status(), Instant.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<PlayerState> getState(String guildId) {
        return CompletableFuture.completedFuture(state(guildId));
    }

    private PlayerState state(String guildId) {
        return states.getOrDefault(guildId, PlayerState.stopped(guildId));
    }

    private int currentVolume(String guildId) {
        return state(guildId).volume();
    }
}
