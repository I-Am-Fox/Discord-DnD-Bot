package com.dndmusicbot.bot.audio;

import com.dndmusicbot.shared.domain.PlaybackStatus;
import com.dndmusicbot.shared.domain.TransitionMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NoopAudioEngineTest {
    @Test
    void simulatesPlaybackState() {
        NoopAudioEngine engine = new NoopAudioEngine();
        TrackRef track = new TrackRef("track-1", "Combat", "scene:combat");

        engine.play("guild-1", track, new PlayOptions(80, true, TransitionMode.FADE_OUT_IN)).join();
        PlayerState playing = engine.getState("guild-1").join();

        assertEquals(PlaybackStatus.PLAYING, playing.status());
        assertEquals(track, playing.activeTrack());
        assertEquals(80, playing.volume());

        engine.stop("guild-1", FadeOptions.none()).join();
        PlayerState stopped = engine.getState("guild-1").join();

        assertEquals(PlaybackStatus.STOPPED, stopped.status());
        assertNull(stopped.activeTrack());
    }
}
