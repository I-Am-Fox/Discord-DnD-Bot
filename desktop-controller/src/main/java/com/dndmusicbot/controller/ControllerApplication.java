package com.dndmusicbot.controller;

import com.dndmusicbot.controller.api.BotApiClient;
import com.dndmusicbot.shared.api.CreateCampaignRequest;
import com.dndmusicbot.shared.api.CreateSceneRequest;
import com.dndmusicbot.shared.api.SceneSwitchRequest;
import com.dndmusicbot.shared.api.VolumeRequest;
import com.dndmusicbot.shared.domain.LoopMode;
import com.dndmusicbot.shared.domain.SceneType;
import com.dndmusicbot.shared.domain.TransitionMode;

import java.net.URI;
import java.util.Locale;

public final class ControllerApplication {
    private ControllerApplication() {
    }

    public static void main(String[] args) {
        ControllerConfig config = ControllerConfig.fromEnvironment();
        BotApiClient client = new BotApiClient(config.serverUrl(), config.apiToken());
        if (args.length == 0) {
            printUsage();
            return;
        }

        try {
            handle(args, client);
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void handle(String[] args, BotApiClient client) {
        String command = args[0].toLowerCase(Locale.ROOT);
        switch (command) {
            case "health" -> System.out.println(client.pretty(client.health()));
            case "campaigns" -> System.out.println(client.pretty(client.campaigns()));
            case "create-campaign" -> {
                requireArgCount(args, 3, "create-campaign <name> <guildId> [defaultVoiceChannelId]");
                String voiceChannelId = args.length > 3 ? args[3] : null;
                System.out.println(client.pretty(client.createCampaign(new CreateCampaignRequest(args[1], args[2], voiceChannelId))));
            }
            case "scenes" -> {
                requireArgCount(args, 2, "scenes <campaignId>");
                System.out.println(client.pretty(client.scenes(args[1])));
            }
            case "create-scene" -> {
                requireArgCount(args, 5, "create-scene <campaignId> <name> <type> <playlistId>");
                CreateSceneRequest request = new CreateSceneRequest(
                    args[1],
                    args[2],
                    SceneType.valueOf(args[3].toUpperCase(Locale.ROOT)),
                    70,
                    TransitionMode.FADE_OUT_IN,
                    TransitionMode.FADE_OUT_IN,
                    LoopMode.LOOP_PLAYLIST,
                    args[4]
                );
                System.out.println(client.pretty(client.createScene(request)));
            }
            case "switch-scene" -> {
                requireArgCount(args, 4, "switch-scene <guildId> <campaignId> <sceneName> [transitionMode]");
                TransitionMode mode = args.length > 4 ? TransitionMode.valueOf(args[4].toUpperCase(Locale.ROOT)) : TransitionMode.SMART_SCENE_SWITCH;
                SceneSwitchRequest request = new SceneSwitchRequest(args[1], args[2], args[3], mode);
                System.out.println(client.pretty(client.switchScene(request)));
            }
            case "state" -> {
                requireArgCount(args, 2, "state <guildId>");
                System.out.println(client.pretty(client.state(args[1])));
            }
            case "volume" -> {
                requireArgCount(args, 3, "volume <guildId> <0-150>");
                System.out.println(client.pretty(client.volume(new VolumeRequest(args[1], Integer.parseInt(args[2])))));
            }
            default -> printUsage();
        }
    }

    private static void requireArgCount(String[] args, int minCount, String usage) {
        if (args.length < minCount) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    private static void printUsage() {
        System.out.println("""
            D&D Music Bot CLI

            Commands:
              health
              campaigns
              create-campaign <name> <guildId> [defaultVoiceChannelId]
              scenes <campaignId>
              create-scene <campaignId> <name> <type> <playlistId>
              switch-scene <guildId> <campaignId> <sceneName> [transitionMode]
              state <guildId>
              volume <guildId> <0-150>
            """);
    }

    private record ControllerConfig(URI serverUrl, String apiToken) {
        static ControllerConfig fromEnvironment() {
            String serverUrl = valueOrDefault("BOT_SERVER_URL", "http://127.0.0.1:8080");
            String apiToken = required("BOT_API_TOKEN");
            return new ControllerConfig(URI.create(ensureTrailingSlash(serverUrl)), apiToken);
        }

        private static String required(String name) {
            String value = System.getenv(name);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " is required");
            }
            return value.trim();
        }

        private static String valueOrDefault(String name, String defaultValue) {
            String value = System.getenv(name);
            return value == null || value.isBlank() ? defaultValue : value.trim();
        }

        private static String ensureTrailingSlash(String value) {
            return value.endsWith("/") ? value : value + "/";
        }
    }
}
