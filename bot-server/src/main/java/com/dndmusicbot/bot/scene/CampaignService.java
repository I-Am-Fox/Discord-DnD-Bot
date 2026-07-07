package com.dndmusicbot.bot.scene;

import com.dndmusicbot.bot.persistence.CampaignRepository;
import com.dndmusicbot.shared.api.CampaignDto;
import com.dndmusicbot.shared.api.CreateCampaignRequest;
import com.dndmusicbot.shared.domain.ValueChecks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CampaignService {
    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public List<CampaignDto> listCampaigns() {
        return campaignRepository.findAll();
    }

    public Optional<CampaignDto> findById(String id) {
        return campaignRepository.findById(id);
    }

    public CampaignDto createCampaign(CreateCampaignRequest request) {
        String name = ValueChecks.requireText(request.name(), "name");
        String guildId = ValueChecks.requireText(request.guildId(), "guildId");
        String voiceChannelId = optionalText(request.defaultVoiceChannelId());

        CampaignDto campaign = new CampaignDto(UUID.randomUUID().toString(), name, guildId, voiceChannelId);
        return campaignRepository.save(campaign);
    }

    private static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
