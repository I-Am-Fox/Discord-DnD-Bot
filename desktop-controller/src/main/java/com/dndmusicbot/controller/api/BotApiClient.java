package com.dndmusicbot.controller.api;

import com.dndmusicbot.shared.api.CampaignDto;
import com.dndmusicbot.shared.api.CreateCampaignRequest;
import com.dndmusicbot.shared.api.CreateSceneRequest;
import com.dndmusicbot.shared.api.HealthResponse;
import com.dndmusicbot.shared.api.PlaybackStateDto;
import com.dndmusicbot.shared.api.SceneProfileDto;
import com.dndmusicbot.shared.api.SceneSwitchRequest;
import com.dndmusicbot.shared.api.VolumeRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class BotApiClient {
    private final URI baseUri;
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BotApiClient(URI baseUri, String apiToken) {
        this.baseUri = baseUri;
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public HealthResponse health() {
        return get("/api/health", HealthResponse.class, false);
    }

    public List<CampaignDto> campaigns() {
        return get("/api/campaigns", new TypeReference<>() {
        });
    }

    public CampaignDto createCampaign(CreateCampaignRequest request) {
        return post("/api/campaigns", request, CampaignDto.class);
    }

    public List<SceneProfileDto> scenes(String campaignId) {
        String path = "/api/scenes?campaignId=" + encode(campaignId);
        return get(path, new TypeReference<>() {
        });
    }

    public SceneProfileDto createScene(CreateSceneRequest request) {
        return post("/api/scenes", request, SceneProfileDto.class);
    }

    public PlaybackStateDto switchScene(SceneSwitchRequest request) {
        return post("/api/playback/scene-switch", request, PlaybackStateDto.class);
    }

    public PlaybackStateDto state(String guildId) {
        return get("/api/playback/state?guildId=" + encode(guildId), PlaybackStateDto.class, true);
    }

    public PlaybackStateDto volume(VolumeRequest request) {
        return post("/api/playback/volume", request, PlaybackStateDto.class);
    }

    public String pretty(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to render response", ex);
        }
    }

    private <T> T get(String path, Class<T> responseType, boolean authenticated) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(resolve(path))
            .timeout(Duration.ofSeconds(10))
            .GET();
        if (authenticated) {
            builder.header("Authorization", "Bearer " + apiToken);
        }
        return send(builder.build(), responseType);
    }

    private <T> T get(String path, TypeReference<T> responseType) {
        HttpRequest request = authorizedBuilder(path)
            .GET()
            .build();
        return send(request, responseType);
    }

    private <T> T post(String path, Object requestBody, Class<T> responseType) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = authorizedBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            return send(request, responseType);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to serialize request", ex);
        }
    }

    private HttpRequest.Builder authorizedBuilder(String path) {
        return HttpRequest.newBuilder(resolve(path))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization", "Bearer " + apiToken);
    }

    private <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response);
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException ex) {
            throw new IllegalStateException("API request failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("API request interrupted", ex);
        }
    }

    private <T> T send(HttpRequest request, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response);
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException ex) {
            throw new IllegalStateException("API request failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("API request interrupted", ex);
        }
    }

    private void ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("API returned HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private URI resolve(String path) {
        return baseUri.resolve(path);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
