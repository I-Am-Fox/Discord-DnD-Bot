package com.dndmusicbot.bot.persistence;

import com.dndmusicbot.bot.config.JsonSupport;
import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSceneRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsScenesByCampaignAndName() {
        JsonSceneRepository repository = new JsonSceneRepository(tempDir.resolve("scenes.json"), JsonSupport.objectMapper());
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

        repository.save(scene);

        List<SceneProfileDto> scenes = repository.findByCampaignId("campaign-1");
        assertEquals(List.of(scene), scenes);
        assertEquals(scene, repository.findByName("campaign-1", "dungeon tension").orElseThrow());
        assertTrue(repository.findByName("campaign-1", "missing").isEmpty());
    }
}
