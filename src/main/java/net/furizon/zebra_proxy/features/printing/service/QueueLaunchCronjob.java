package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueLaunchCronjob {
    @NotNull
    private final JobManagementService jobManagementService;

    @Scheduled(fixedRateString = "${worker.delay}", timeUnit = TimeUnit.SECONDS)
    public void launch() {
        log.info("Launching queue cronjob");
        List<QueuePair> queues = jobManagementService.getQueues();
        queues.forEach(jobManagementService::runAsync);
    }
}
