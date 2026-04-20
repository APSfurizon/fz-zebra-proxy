package net.furizon.zebra_proxy.features.printing.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintConfigService {
    private static final ReentrantLock MUTEX = new ReentrantLock(true);

    @NotNull
    private final Map<QueuePair, PrinterSettings> printerMap = new HashMap<>();


    @NotNull
    private final PrintConfigPersist printConfigPersist;


    public @Nullable PrinterSettings getPrinterNamePerQueuePair(@NotNull final QueuePair queuePair) {
        try {
            MUTEX.lock();
            return printerMap.get(queuePair);
        } finally {
            MUTEX.unlock();
        }
    }

    public @NotNull Set<Long> getOperators() {
        try {
            MUTEX.lock();
            return printerMap.keySet().stream().map(QueuePair::getOperatorID).collect(Collectors.toUnmodifiableSet());
        } finally {
            MUTEX.unlock();
        }
    }

    public void addConfig(@NotNull QueuePair queuePair, @NotNull PrinterSettings printerSettings) {
        try {
            MUTEX.lock();
            printerMap.put(queuePair, printerSettings);
            printConfigPersist.save(printerMap);
        } finally {
            MUTEX.unlock();
        }
    }
    public void overrideConfig(@NotNull Map<QueuePair, PrinterSettings> newConfigs) {
        try {
            MUTEX.lock();
            printerMap.clear();
            printerMap.putAll(newConfigs);
            printConfigPersist.save(printerMap);
        } finally {
            MUTEX.unlock();
        }
    }
    public @NotNull Map<QueuePair, PrinterSettings> getConfig() {
        try {
            MUTEX.lock();
            return new HashMap<>(printerMap);
        } finally {
            MUTEX.unlock();
        }
    }

    @PostConstruct
    public void init() {
        log.info("Loading printer config");
        try {
            MUTEX.lock();
            printerMap.putAll(printConfigPersist.load());
        } finally {
            MUTEX.unlock();
        }
    }
}
