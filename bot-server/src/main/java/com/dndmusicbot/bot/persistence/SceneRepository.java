package com.dndmusicbot.bot.persistence;

import com.dndmusicbot.shared.api.SceneProfileDto;

import java.util.List;
import java.util.Optional;

public interface SceneRepository {
    List<SceneProfileDto> findAll();

    List<SceneProfileDto> findByCampaignId(String campaignId);

    Optional<SceneProfileDto> findByName(String campaignId, String name);

    Optional<SceneProfileDto> findById(String id);

    SceneProfileDto save(SceneProfileDto scene);
}
