package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrintRequest;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobManagementService {

    private final Map<QueuePair, AtomicBoolean> queuesLocks = new ConcurrentHashMap<>();
    private final Map<QueuePair, Queue<PrintIdContentPair>> queues = new ConcurrentHashMap<>();

    @NotNull
    private final PrintingService printingService;

    public synchronized @NotNull QueuePair submitJob(@NotNull PrintRequest printRequest) {
        var pair = printRequest.toQueuePair();
        var queue = queues.computeIfAbsent(pair, p -> {
            getLockForQueue(p);
            return new ConcurrentLinkedQueue<PrintIdContentPair>();
        });
        queue.add(printRequest.toIdContentPair());
        return pair;
    }

    private synchronized @NotNull AtomicBoolean getLockForQueue(@NotNull QueuePair pair) {
        var ret = queuesLocks.computeIfAbsent(pair, _ -> new AtomicBoolean(false));
        return ret;
    }

    @Async
    public void runAsync(@NotNull QueuePair pair) {
        run(pair);
    }
    public void run(@NotNull QueuePair pair) {
        var queuePair = getLockForQueue(pair);
        boolean isLocked = queuePair.getAndSet(true);
        if (isLocked) {
            log.debug("Queue {} is already locked", pair);
            return;
        }
        log.debug("Locked queue {}", pair);

        try {

            var queue = queues.get(pair);
            if (queue == null) {
                log.warn("Queue {} not found", pair);
                return;
            }
            PrintIdContentPair job;
            while ((job = queue.poll()) != null) {
                printingService.invoke(job);
            }

        } finally {
            log.debug("Unlocking queue {}", pair);
            queuePair.set(false);
        }
    }
}
