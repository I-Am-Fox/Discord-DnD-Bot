package com.dndmusicbot.bot.api;

import com.dndmusicbot.bot.config.BotConfig;
import com.dndmusicbot.bot.errors.MusicBotException;
import com.dndmusicbot.bot.errors.SceneNotFoundException;
import com.dndmusicbot.bot.playback.PlaybackService;
import com.dndmusicbot.bot.scene.CampaignService;
import com.dndmusicbot.bot.scene.SceneService;
import com.dndmusicbot.shared.api.ApiErrorResponse;
import com.dndmusicbot.shared.api.CreateCampaignRequest;
import com.dndmusicbot.shared.api.CreateSceneRequest;
import com.dndmusicbot.shared.api.HealthResponse;
import com.dndmusicbot.shared.api.JoinRequest;
import com.dndmusicbot.shared.api.PlayRequest;
import com.dndmusicbot.shared.api.PlaybackCommandRequest;
import com.dndmusicbot.shared.api.SceneSwitchRequest;
import com.dndmusicbot.shared.api.VolumeRequest;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.TooManyRequestsResponse;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;

public class ApiServer {
    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);

    private final BotConfig config;
    private final CampaignService campaignService;
    private final SceneService sceneService;
    private final PlaybackService playbackService;
    private final ApiAuthenticator authenticator;
    private final ApiRateLimiter rateLimiter;
    private final Javalin app;

    public ApiServer(
        BotConfig config,
        CampaignService campaignService,
        SceneService sceneService,
        PlaybackService playbackService
    ) {
        this.config = config;
        this.campaignService = campaignService;
        this.sceneService = sceneService;
        this.playbackService = playbackService;
        this.authenticator = new ApiAuthenticator(config.apiToken());
        this.rateLimiter = new ApiRateLimiter(120, 60_000, Clock.systemUTC());
        this.app = createApp();
    }

    public void start() {
        app.start(config.apiHost(), config.apiPort());
        log.info("Bot API listening on {}:{}", config.apiHost(), config.apiPort());
    }

    public void stop() {
        app.stop();
    }

    public Javalin app() {
        return app;
    }

    private Javalin createApp() {
        Javalin javalin = Javalin.create(javalinConfig -> {
            javalinConfig.routes.beforeMatched(this::authorize);
            javalinConfig.routes.get("/api/health", ctx -> ctx.json(new HealthResponse("ok", "dnd-music-bot", Instant.now())));
            javalinConfig.routes.get("/api/campaigns", ctx -> ctx.json(campaignService.listCampaigns()));
            javalinConfig.routes.post("/api/campaigns", ctx -> {
                CreateCampaignRequest request = ctx.bodyAsClass(CreateCampaignRequest.class);
                ctx.status(201).json(campaignService.createCampaign(request));
            });
            javalinConfig.routes.get("/api/scenes", ctx -> {
                String campaignId = ctx.queryParam("campaignId");
                ctx.json(sceneService.listScenes(campaignId));
            });
            javalinConfig.routes.post("/api/scenes", ctx -> {
                CreateSceneRequest request = ctx.bodyAsClass(CreateSceneRequest.class);
                ctx.status(201).json(sceneService.createScene(request));
            });
            javalinConfig.routes.post("/api/playback/join", ctx -> ctx.json(playbackService.join(ctx.bodyAsClass(JoinRequest.class))));
            javalinConfig.routes.post("/api/playback/leave", ctx -> ctx.json(playbackService.leave(ctx.bodyAsClass(PlaybackCommandRequest.class))));
            javalinConfig.routes.post("/api/playback/play", ctx -> ctx.json(playbackService.play(ctx.bodyAsClass(PlayRequest.class))));
            javalinConfig.routes.post("/api/playback/stop", ctx -> ctx.json(playbackService.stop(ctx.bodyAsClass(PlaybackCommandRequest.class))));
            javalinConfig.routes.post("/api/playback/pause", ctx -> ctx.json(playbackService.pause(ctx.bodyAsClass(PlaybackCommandRequest.class))));
            javalinConfig.routes.post("/api/playback/resume", ctx -> ctx.json(playbackService.resume(ctx.bodyAsClass(PlaybackCommandRequest.class))));
            javalinConfig.routes.post("/api/playback/volume", ctx -> ctx.json(playbackService.setVolume(ctx.bodyAsClass(VolumeRequest.class))));
            javalinConfig.routes.post("/api/playback/scene-switch", ctx -> ctx.json(playbackService.switchScene(ctx.bodyAsClass(SceneSwitchRequest.class))));
            javalinConfig.routes.post("/api/playback/stinger", ctx -> {
                throw new NotFoundResponse("Stinger playback is not implemented in Phase 1");
            });
            javalinConfig.routes.get("/api/playback/state", ctx -> ctx.json(playbackService.state(ctx.queryParam("guildId"))));
            javalinConfig.routes.get("/api/events", ctx -> ctx.status(204));
            javalinConfig.routes.exception(IllegalArgumentException.class, (ex, ctx) -> writeError(ctx, 400, "BAD_REQUEST", ex.getMessage()));
            javalinConfig.routes.exception(SceneNotFoundException.class, (ex, ctx) -> writeError(ctx, 404, "SCENE_NOT_FOUND", ex.getMessage()));
            javalinConfig.routes.exception(MusicBotException.class, (ex, ctx) -> {
                log.warn("Application error: {}", ex.getMessage(), ex);
                writeError(ctx, 500, "MUSIC_BOT_ERROR", "Unable to complete the request.");
            });
            javalinConfig.routes.exception(Exception.class, (ex, ctx) -> {
                log.error("Unhandled API error", ex);
                writeError(ctx, 500, "INTERNAL_ERROR", "Internal server error.");
            });
        });
        return javalin;
    }

    private void authorize(Context ctx) {
        if (!ctx.path().startsWith("/api/") || "/api/health".equals(ctx.path())) {
            return;
        }
        if (!rateLimiter.allow(ctx.ip())) {
            log.warn("Rate limit exceeded for {}", ctx.ip());
            throw new TooManyRequestsResponse();
        }
        boolean authorized = authenticator.isAuthorized(ctx.header("Authorization"), ctx.header("X-Api-Token"));
        if (!authorized) {
            log.warn("Rejected unauthorized API request from {} to {}", ctx.ip(), ctx.path());
            throw new UnauthorizedResponse();
        }
    }

    private static void writeError(Context ctx, int status, String code, String message) {
        ctx.status(status).json(new ApiErrorResponse(code, message));
    }
}
