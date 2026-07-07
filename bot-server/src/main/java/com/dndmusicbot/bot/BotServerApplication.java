package com.dndmusicbot.bot;

import com.dndmusicbot.bot.api.ApiServer;
import com.dndmusicbot.bot.audio.NoopAudioEngine;
import com.dndmusicbot.bot.config.BotConfig;
import com.dndmusicbot.bot.config.BotConfigLoader;
import com.dndmusicbot.bot.config.JsonSupport;
import com.dndmusicbot.bot.persistence.CampaignRepository;
import com.dndmusicbot.bot.persistence.JsonCampaignRepository;
import com.dndmusicbot.bot.persistence.JsonSceneRepository;
import com.dndmusicbot.bot.persistence.SceneRepository;
import com.dndmusicbot.bot.playback.PlaybackService;
import com.dndmusicbot.bot.scene.CampaignService;
import com.dndmusicbot.bot.scene.SceneService;
import com.dndmusicbot.bot.scene.TransitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

public final class BotServerApplication {
    private static final Logger log = LoggerFactory.getLogger(BotServerApplication.class);

    private BotServerApplication() {
    }

    public static void main(String[] args) throws IOException {
        BotConfig config = BotConfigLoader.loadFromEnvironment();
        Files.createDirectories(config.dataDir());
        log.info("Starting D&D Music Bot server with {}", config.safeSummary());

        ObjectMapper objectMapper = JsonSupport.objectMapper();
        CampaignRepository campaignRepository = new JsonCampaignRepository(config.dataDir().resolve("campaigns.json"), objectMapper);
        SceneRepository sceneRepository = new JsonSceneRepository(config.dataDir().resolve("scenes.json"), objectMapper);

        CampaignService campaignService = new CampaignService(campaignRepository);
        SceneService sceneService = new SceneService(campaignService, sceneRepository);
        TransitionService transitionService = new TransitionService();
        PlaybackService playbackService = new PlaybackService(new NoopAudioEngine(), sceneService, transitionService);

        ApiServer apiServer = new ApiServer(config, campaignService, sceneService, playbackService);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping D&D Music Bot server");
            apiServer.stop();
        }, "bot-server-shutdown"));
        apiServer.start();
    }
}
