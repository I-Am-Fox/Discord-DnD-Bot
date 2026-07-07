package com.dndmusicbot.bot.persistence;

import com.dndmusicbot.bot.errors.MusicBotException;
import com.dndmusicbot.shared.api.CampaignDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JsonCampaignRepository implements CampaignRepository {
    private final Path file;
    private final ObjectMapper objectMapper;
    private final Object lock = new Object();

    public JsonCampaignRepository(Path file, ObjectMapper objectMapper) {
        this.file = file;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CampaignDto> findAll() {
        synchronized (lock) {
            return readFile().campaigns().stream()
                .sorted(Comparator.comparing(CampaignDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        }
    }

    @Override
    public Optional<CampaignDto> findById(String id) {
        synchronized (lock) {
            return readFile().campaigns().stream()
                .filter(campaign -> campaign.id().equals(id))
                .findFirst();
        }
    }

    @Override
    public CampaignDto save(CampaignDto campaign) {
        synchronized (lock) {
            List<CampaignDto> campaigns = new ArrayList<>(readFile().campaigns());
            campaigns.removeIf(existing -> existing.id().equals(campaign.id()));
            campaigns.add(campaign);
            writeFile(new CampaignFile(campaigns));
            return campaign;
        }
    }

    private CampaignFile readFile() {
        if (!Files.exists(file)) {
            return new CampaignFile(List.of());
        }
        try {
            CampaignFile data = objectMapper.readValue(file.toFile(), CampaignFile.class);
            return data.campaigns() == null ? new CampaignFile(List.of()) : data;
        } catch (IOException ex) {
            throw new MusicBotException("Unable to read campaign data", ex);
        }
    }

    private void writeFile(CampaignFile data) {
        try {
            Files.createDirectories(file.getParent());
            Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), data);
            moveAtomically(tempFile, file);
        } catch (IOException ex) {
            throw new MusicBotException("Unable to write campaign data", ex);
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record CampaignFile(List<CampaignDto> campaigns) {
    }
}
