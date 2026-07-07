package com.dndmusicbot.bot.persistence;

import com.dndmusicbot.shared.api.CampaignDto;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository {
    List<CampaignDto> findAll();

    Optional<CampaignDto> findById(String id);

    CampaignDto save(CampaignDto campaign);
}
