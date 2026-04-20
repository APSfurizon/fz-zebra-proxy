package net.furizon.zebra_proxy.features.printing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrintConfigPersist {
    private static final Path CONFIG_FILE = Paths.get("data/config.json");

    @NotNull
    private final ObjectMapper objectMapper;

    public void save(@NotNull Map<QueuePair, PrinterSettings> map) {
        log.info("Saving config to disk");
        try {
            String config = objectMapper.writeValueAsString(map);
            Files.write(CONFIG_FILE, config.getBytes());
        } catch (JsonProcessingException e) {
            log.error("Failed to convert config while saving it to disk", e);
        } catch (IOException e) {
            log.error("Failed to write config to disk", e);
        }
    }

    public @NotNull Map<QueuePair, PrinterSettings> load() {
        log.info("Loading config from disk");
        try {
            return objectMapper.readValue(Files.readString(CONFIG_FILE), new TypeReference<Map<QueuePair, PrinterSettings>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to convert config while loading it from disk", e);
            return Map.of();
            //throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Failed to read config from disk", e);
            return Map.of();
            //throw new RuntimeException(e);
        }
    }
}
