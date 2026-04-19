package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintConfigService {
    private final Map<QueuePair, PrinterSettings> printerMap = new HashMap<>();


    public @Nullable PrinterSettings getPrinterNamePerQueuePair(@NotNull final QueuePair queuePair) {
        return printerMap.get(queuePair);
    }


}
