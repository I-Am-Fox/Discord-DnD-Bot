package com.dndmusicbot.bot.scene;

import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;

public class TransitionService {
    public TransitionMode resolve(SceneProfileDto currentScene, SceneProfileDto targetScene, TransitionMode requestedMode) {
        TransitionMode mode = requestedMode == null ? targetScene.transitionIn() : requestedMode;
        if (mode != TransitionMode.SMART_SCENE_SWITCH) {
            return mode;
        }
        return smartMode(currentScene == null ? null : currentScene.type(), targetScene.type());
    }

    private TransitionMode smartMode(SceneType currentType, SceneType targetType) {
        if (currentType == null) {
            return TransitionMode.FADE_OUT_IN;
        }
        if (currentType == SceneType.EXPLORATION && targetType == SceneType.TENSION) {
            return TransitionMode.CROSSFADE;
        }
        if (currentType == SceneType.TENSION && targetType == SceneType.COMBAT) {
            return TransitionMode.FADE_OUT_IN;
        }
        if (currentType == SceneType.COMBAT && targetType == SceneType.BOSS) {
            return TransitionMode.NONE;
        }
        if (currentType == SceneType.COMBAT && targetType == SceneType.VICTORY) {
            return TransitionMode.FADE_OUT_IN;
        }
        return TransitionMode.FADE_OUT_IN;
    }
}
