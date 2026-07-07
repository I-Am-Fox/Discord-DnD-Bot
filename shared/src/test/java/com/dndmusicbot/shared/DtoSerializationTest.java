package com.dndmusicbot.shared;

import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoSerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sceneProfileRoundTripsAsJson() throws Exception {
        SceneProfileDto scene = new SceneProfileDto(
            "scene-1",
            "campaign-1",
            "Dungeon Tension",
            SceneType.TENSION,
            60,
            TransitionMode.FADE_OUT_IN,
            TransitionMode.FADE_OUT_IN,
            LoopMode.LOOP_PLAYLIST,
            "playlist-1"
        );

        String json = objectMapper.writeValueAsString(scene);
        SceneProfileDto restored = objectMapper.readValue(json, SceneProfileDto.class);

        assertEquals(scene, restored);
    }
}
