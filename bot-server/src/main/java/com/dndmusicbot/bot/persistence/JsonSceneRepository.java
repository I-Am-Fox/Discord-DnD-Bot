package com.dndmusicbot.bot.persistence;

import com.dndmusicbot.bot.errors.MusicBotException;
import com.dndmusicbot.shared.api.SceneProfileDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class JsonSceneRepository implements SceneRepository {
    private final Path file;
    private final ObjectMapper objectMapper;
    private final Object lock = new Object();

    public JsonSceneRepository(Path file, ObjectMapper objectMapper) {
        this.file = file;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SceneProfileDto> findAll() {
        synchronized (lock) {
            return sorted(readFile().scenes());
        }
    }

    @Override
    public List<SceneProfileDto> findByCampaignId(String campaignId) {
        synchronized (lock) {
            return sorted(readFile().scenes().stream()
                .filter(scene -> scene.campaignId().equals(campaignId))
                .toList());
        }
    }

    @Override
    public Optional<SceneProfileDto> findByName(String campaignId, String name) {
        String normalized = normalize(name);
        synchronized (lock) {
            return readFile().scenes().stream()
                .filter(scene -> scene.campaignId().equals(campaignId))
                .filter(scene -> normalize(scene.name()).equals(normalized))
                .findFirst();
        }
    }

    @Override
    public Optional<SceneProfileDto> findById(String id) {
        synchronized (lock) {
            return readFile().scenes().stream()
                .filter(scene -> scene.id().equals(id))
                .findFirst();
        }
    }

    @Override
    public SceneProfileDto save(SceneProfileDto scene) {
        synchronized (lock) {
            List<SceneProfileDto> scenes = new ArrayList<>(readFile().scenes());
            scenes.removeIf(existing -> existing.id().equals(scene.id()));
            scenes.add(scene);
            writeFile(new SceneFile(scenes));
            return scene;
        }
    }

    private List<SceneProfileDto> sorted(List<SceneProfileDto> scenes) {
        return scenes.stream()
            .sorted(Comparator.comparing(SceneProfileDto::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private SceneFile readFile() {
        if (!Files.exists(file)) {
            return new SceneFile(List.of());
        }
        try {
            SceneFile data = objectMapper.readValue(file.toFile(), SceneFile.class);
            return data.scenes() == null ? new SceneFile(List.of()) : data;
        } catch (IOException ex) {
            throw new MusicBotException("Unable to read scene data", ex);
        }
    }

    private void writeFile(SceneFile data) {
        try {
            Files.createDirectories(file.getParent());
            Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), data);
            moveAtomically(tempFile, file);
        } catch (IOException ex) {
            throw new MusicBotException("Unable to write scene data", ex);
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record SceneFile(List<SceneProfileDto> scenes) {
    }
}
