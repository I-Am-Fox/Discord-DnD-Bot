package com.dndmusicbot.bot.playback;

import com.dndmusicbot.bot.audio.AudioEngine;
import com.dndmusicbot.bot.audio.FadeOptions;
import com.dndmusicbot.bot.audio.PlayOptions;
import com.dndmusicbot.bot.audio.PlayerState;
import com.dndmusicbot.bot.audio.TrackRef;
import com.dndmusicbot.bot.scene.SceneService;
import com.dndmusicbot.bot.scene.TransitionService;
import com.dndmusicbot.shared.api.JoinRequest;
import com.dndmusicbot.shared.api.PlayRequest;
import com.dndmusicbot.shared.api.PlaybackCommandRequest;
import com.dndmusicbot.shared.api.PlaybackStateDto;
import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.api.SceneSwitchRequest;
import com.dndmusicbot.shared.api.VolumeRequest;
import com.dndmusicbot.shared.domain.PlaybackStatus;
import com.dndmusicbot.shared.domain.TransitionMode;
import com.dndmusicbot.shared.domain.ValueChecks;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlaybackService {
    private final AudioEngine audioEngine;
    private final SceneService sceneService;
    private final TransitionService transitionService;
    private final Map<String, PlaybackStateDto> sessions = new ConcurrentHashMap<>();

    public PlaybackService(AudioEngine audioEngine, SceneService sceneService, TransitionService transitionService) {
        this.audioEngine = audioEngine;
        this.sceneService = sceneService;
        this.transitionService = transitionService;
    }

    public PlaybackStateDto join(JoinRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        String voiceChannelId = ValueChecks.requireText(request.voiceChannelId(), "voiceChannelId");
        audioEngine.connect(guildId, voiceChannelId).join();
        return mergeAudioState(guildId, PlaybackStatus.STOPPED);
    }

    public PlaybackStateDto leave(PlaybackCommandRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        audioEngine.disconnect(guildId).join();
        return mergeAudioState(guildId, PlaybackStatus.DISCONNECTED);
    }

    public PlaybackStateDto play(PlayRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        String queryOrUrl = ValueChecks.requireText(request.queryOrUrl(), "queryOrUrl");
        TrackRef track = audioEngine.resolve(queryOrUrl).join();
        audioEngine.play(guildId, track, new PlayOptions(currentVolume(guildId), false, TransitionMode.NONE)).join();
        return update(guildId, current(guildId), PlaybackStatus.PLAYING, track.id(), null);
    }

    public PlaybackStateDto stop(PlaybackCommandRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        audioEngine.stop(guildId, FadeOptions.none()).join();
        return update(guildId, current(guildId), PlaybackStatus.STOPPED, null, null);
    }

    public PlaybackStateDto pause(PlaybackCommandRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        audioEngine.pause(guildId).join();
        return update(guildId, current(guildId), PlaybackStatus.PAUSED, current(guildId).activeTrackId(), null);
    }

    public PlaybackStateDto resume(PlaybackCommandRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        audioEngine.resume(guildId).join();
        PlaybackStateDto current = current(guildId);
        PlaybackStatus status = current.activeTrackId() == null && current.activeSceneId() == null
            ? PlaybackStatus.STOPPED
            : PlaybackStatus.PLAYING;
        return update(guildId, current, status, current.activeTrackId(), null);
    }

    public PlaybackStateDto setVolume(VolumeRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        int volume = ValueChecks.requireVolume(request.volume());
        audioEngine.setVolume(guildId, volume).join();
        PlaybackStateDto current = current(guildId);
        PlaybackStateDto updated = new PlaybackStateDto(
            guildId,
            current.campaignId(),
            current.activeSceneId(),
            current.activeSceneName(),
            current.activeTrackId(),
            current.queue(),
            volume,
            current.status(),
            current.lastTransition(),
            Instant.now()
        );
        sessions.put(guildId, updated);
        return updated;
    }

    public PlaybackStateDto switchScene(SceneSwitchRequest request) {
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        SceneProfileDto targetScene = sceneService.findByName(request.campaignId(), request.sceneName());
        PlaybackStateDto current = current(guildId);
        SceneProfileDto currentScene = current.activeSceneName() == null
            ? null
            : sceneService.findByName(current.campaignId(), current.activeSceneName());
        TransitionMode selectedMode = transitionService.resolve(currentScene, targetScene, request.transitionMode());
        TrackRef track = new TrackRef("scene-" + targetScene.id(), targetScene.name(), "scene:" + targetScene.playlistId());

        audioEngine.play(guildId, track, new PlayOptions(targetScene.defaultVolume(), true, selectedMode)).join();

        PlaybackStateDto updated = new PlaybackStateDto(
            guildId,
            targetScene.campaignId(),
            targetScene.id(),
            targetScene.name(),
            track.id(),
            current.queue(),
            targetScene.defaultVolume(),
            PlaybackStatus.PLAYING,
            selectedMode,
            Instant.now()
        );
        sessions.put(guildId, updated);
        return updated;
    }

    public PlaybackStateDto state(String guildId) {
        return current(ValueChecks.requireText(guildId, "guildId"));
    }

    private PlaybackStateDto mergeAudioState(String guildId, PlaybackStatus fallbackStatus) {
        PlayerState playerState = audioEngine.getState(guildId).join();
        PlaybackStateDto current = current(guildId);
        PlaybackStatus status = playerState.status() == null ? fallbackStatus : playerState.status();
        return update(guildId, current, status, playerState.activeTrack() == null ? current.activeTrackId() : playerState.activeTrack().id(), null);
    }

    private PlaybackStateDto update(
        String guildId,
        PlaybackStateDto current,
        PlaybackStatus status,
        String activeTrackId,
        TransitionMode transitionMode
    ) {
        PlaybackStateDto updated = new PlaybackStateDto(
            guildId,
            current.campaignId(),
            status == PlaybackStatus.STOPPED || status == PlaybackStatus.DISCONNECTED ? null : current.activeSceneId(),
            status == PlaybackStatus.STOPPED || status == PlaybackStatus.DISCONNECTED ? null : current.activeSceneName(),
            activeTrackId,
            current.queue(),
            current.volume(),
            status,
            transitionMode == null ? current.lastTransition() : transitionMode,
            Instant.now()
        );
        sessions.put(guildId, updated);
        return updated;
    }

    private PlaybackStateDto current(String guildId) {
        return sessions.computeIfAbsent(guildId, key -> new PlaybackStateDto(
            key,
            null,
            null,
            null,
            null,
            List.of(),
            70,
            PlaybackStatus.STOPPED,
            TransitionMode.NONE,
            Instant.now()
        ));
    }

    private int currentVolume(String guildId) {
        return current(guildId).volume();
    }
}
