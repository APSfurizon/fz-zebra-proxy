package net.furizon.zebra_proxy.features.printing.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintRequest;
import net.furizon.zebra_proxy.features.printing.service.JobManagementService;
import net.furizon.zebra_proxy.infrastructure.security.annotation.InternalAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/print")
@RequiredArgsConstructor
public class PrintingController {
    @NotNull
    private final JobManagementService jobManagementService;

    @InternalAuthorize
    @GetMapping("/ping")
    public boolean internalPing() {
        return true;
    }

    @InternalAuthorize
    @PostMapping("/")
    public void submitJob(@Valid @RequestBody @NotNull PrintRequest request) {
        log.info("Received print request: {}", request);
        var pair = jobManagementService.submitJob(request);
        jobManagementService.runAsync(pair);
    }
}
