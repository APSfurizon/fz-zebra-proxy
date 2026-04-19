package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrintConfigPersist {
    //TODO
    public void save(@NotNull Map<QueuePair, PrinterSettings> map) {

    }

    public @NotNull Map<QueuePair, PrinterSettings> load() {
        return Map.of();
    }
}
