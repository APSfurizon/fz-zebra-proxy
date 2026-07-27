package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterIdentifier;
import net.furizon.zebra_proxy.features.printing.service.printers.PrinterService;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobManagementService {

    private final Map<PrinterIdentifier, AtomicBoolean> queuesLocks = new ConcurrentHashMap<>();
    private final Map<PrinterIdentifier, Queue<Pair<byte[], PrintIdContentPair>>> queues = new ConcurrentHashMap<>();

    public synchronized void submitJob(@NotNull PrinterIdentifier printer, @NotNull PrintIdContentPair printId, byte[] printContent) {
        var queue = queues.computeIfAbsent(printer, p -> {
            getLockForQueue(p);
            return new ConcurrentLinkedQueue<Pair<byte[], PrintIdContentPair>>();
        });
        queue.add(Pair.of(printContent, printId));
        log.debug("Added job {} to queue {}. Queue length = {}", printId.getPrintId(), printer, queue.size());
    }

    private synchronized @NotNull AtomicBoolean getLockForQueue(@NotNull PrinterIdentifier printer) {
        var ret = queuesLocks.computeIfAbsent(printer, _ -> new AtomicBoolean(false));
        return ret;
    }

    public List<PrinterIdentifier> getQueues() {
        return new ArrayList<>(queues.keySet());
    }

    @Async
    public void runAsync(@NotNull PrinterIdentifier printer) {
        run(printer);
    }
    public void run(@NotNull PrinterIdentifier printer) {
        var queuePair = getLockForQueue(printer);
        boolean isLocked = queuePair.getAndSet(true);
        if (isLocked) {
            log.debug("Queue {} is already locked", printer);
            return;
        }
        log.debug("Locked queue {}", printer);

        try {

            var queue = queues.get(printer);
            if (queue == null) {
                log.warn("Queue {} not found", printer);
                return;
            }
            PrinterService printerService = PrinterService.getPrinterService(printer.getPrinterName());
            Pair<byte[], PrintIdContentPair> job;
            boolean jobDone = false;
            while ((job = queue.poll()) != null) {
                log.debug("Printing job {} of queue {}. Queue length = {}", job.getRight().getPrintId(), printer, queue.size());
                printerService.printPdf(job.getLeft(), job.getRight(), printer);
                jobDone = true;
            }
            if (jobDone) {
                log.debug("Queue {} is now empty", printer);
                printerService.queueDone(printer);
            }


        } finally {
            log.debug("Unlocking queue {}", printer);
            queuePair.set(false);
        }
    }
}
