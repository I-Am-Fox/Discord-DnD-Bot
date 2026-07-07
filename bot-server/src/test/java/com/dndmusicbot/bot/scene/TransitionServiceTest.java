package com.dndmusicbot.bot.scene;

import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransitionServiceTest {
    private final TransitionService transitionService = new TransitionService();

    @Test
    void smartSwitchCrossfadesExplorationToTension() {
        SceneProfileDto current = scene("forest", SceneType.EXPLORATION);
        SceneProfileDto target = scene("dungeon", SceneType.TENSION);

        TransitionMode mode = transitionService.resolve(current, target, TransitionMode.SMART_SCENE_SWITCH);

        assertEquals(TransitionMode.CROSSFADE, mode);
    }

    @Test
    void explicitModeWinsOverSceneDefault() {
        SceneProfileDto target = scene("combat", SceneType.COMBAT);

        TransitionMode mode = transitionService.resolve(null, target, TransitionMode.NONE);

        assertEquals(TransitionMode.NONE, mode);
    }

    private static SceneProfileDto scene(String name, SceneType type) {
        return new SceneProfileDto(
            name,
            "campaign-1",
            name,
            type,
            70,
            TransitionMode.SMART_SCENE_SWITCH,
            TransitionMode.FADE_OUT_IN,
            LoopMode.LOOP_PLAYLIST,
            "playlist-1"
        );
    }
}
