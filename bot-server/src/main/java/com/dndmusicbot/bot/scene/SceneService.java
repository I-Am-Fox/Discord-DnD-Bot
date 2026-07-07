package com.dndmusicbot.bot.scene;

import com.dndmusicbot.bot.errors.SceneNotFoundException;
import com.dndmusicbot.bot.persistence.SceneRepository;
import com.dndmusicbot.shared.api.CreateSceneRequest;
import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;
import com.dndmusicbot.shared.domain.ValueChecks;

import java.util.List;
import java.util.UUID;

public class SceneService {
    private final CampaignService campaignService;
    private final SceneRepository sceneRepository;

    public SceneService(CampaignService campaignService, SceneRepository sceneRepository) {
        this.campaignService = campaignService;
        this.sceneRepository = sceneRepository;
    }

    public List<SceneProfileDto> listScenes(String campaignId) {
        String cleanCampaignId = ValueChecks.requireText(campaignId, "campaignId");
        return sceneRepository.findByCampaignId(cleanCampaignId);
    }

    public SceneProfileDto findByName(String campaignId, String name) {
        String cleanCampaignId = ValueChecks.requireText(campaignId, "campaignId");
        String cleanName = ValueChecks.requireText(name, "sceneName");
        return sceneRepository.findByName(cleanCampaignId, cleanName)
            .orElseThrow(() -> new SceneNotFoundException("Scene '%s' was not found".formatted(cleanName)));
    }

    public SceneProfileDto createScene(CreateSceneRequest request) {
        String campaignId = ValueChecks.requireText(request.campaignId(), "campaignId");
        campaignService.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("campaignId does not exist"));

        String name = ValueChecks.requireText(request.name(), "name");
        SceneType type = request.type() == null ? SceneType.CUSTOM : request.type();
        int defaultVolume = ValueChecks.requireVolume(request.defaultVolume() == null ? 70 : request.defaultVolume());
        TransitionMode transitionIn = request.transitionIn() == null ? TransitionMode.FADE_OUT_IN : request.transitionIn();
        TransitionMode transitionOut = request.transitionOut() == null ? TransitionMode.FADE_OUT_IN : request.transitionOut();
        LoopMode loopMode = request.loopMode() == null ? LoopMode.LOOP_PLAYLIST : request.loopMode();
        String playlistId = request.playlistId() == null || request.playlistId().isBlank()
            ? "default"
            : request.playlistId().trim();

        SceneProfileDto scene = new SceneProfileDto(
            UUID.randomUUID().toString(),
            campaignId,
            name,
            type,
            defaultVolume,
            transitionIn,
            transitionOut,
            loopMode,
            playlistId
        );
        return sceneRepository.save(scene);
    }
}
