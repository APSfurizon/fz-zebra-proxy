package net.furizon.zebra_proxy.features.printing.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.*;
import net.furizon.zebra_proxy.features.printing.service.OsPrintUtils;
import net.furizon.zebra_proxy.features.printing.service.PrintConfigService;
import net.furizon.zebra_proxy.infrastructure.security.annotation.InternalAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.print.PrintService;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/internal/config")
@RequiredArgsConstructor
public class ConfigController {
    @NotNull
    private final PrintConfigService printConfigService;
    @NotNull
    private final OsPrintUtils osPrintUtils;

    @InternalAuthorize
    @GetMapping("/printers")
    public PrinterListResponse getPrinters() {
        PrintService def = osPrintUtils.findDefaultPrintService();
        return new PrinterListResponse(
                new ArrayList<>(osPrintUtils.getAvailablePrinters()),
                def == null ? null : def.getName()
        );
    }

    @InternalAuthorize
    @GetMapping("/operators")
    public List<Long> getOperators() {
        return new ArrayList<>(printConfigService.getOperators());
    }

    @InternalAuthorize
    @GetMapping("/print-types")
    public List<PrintType> getPrintTypes() {
        return Arrays.asList(PrintType.values());
    }

    @InternalAuthorize
    @PostMapping("/")
    public void setConfig(@NotNull @Valid @RequestBody PrintConfig config) {
        log.info("Setting new config");
        Map<QueuePair, PrinterSettings> parsedConfig = new HashMap<>();
        for (Map.Entry<Long, Map<PrintType, PrinterSettings>> entry : config.getPrinters().entrySet()) {

            long operatorId = entry.getKey();
            var map = entry.getValue();

            for (PrintType printType : PrintType.values()) {

                PrinterSettings printerSettings = map.get(printType);
                if (printerSettings != null) {
                    parsedConfig.put(QueuePair.of(operatorId, printType), printerSettings);
                }
            }
        }
        printConfigService.overrideConfig(parsedConfig);
    }

    @InternalAuthorize
    @GetMapping("/")
    public @NotNull PrintConfig getConfig() {
        log.info("Getting config");
        Map<Long, Map<PrintType, PrinterSettings>> resMap = new TreeMap<>();
        Map<QueuePair, PrinterSettings> config = printConfigService.getConfig();

        for (Map.Entry<QueuePair, PrinterSettings> entry : config.entrySet()) {
            var pair = entry.getKey();
            var settings = entry.getValue();

            var innerMap = resMap.computeIfAbsent(pair.getOperatorID(), id -> new EnumMap<>(PrintType.class));
            innerMap.put(pair.getPrintType(), settings);
        }

        return new PrintConfig(resMap);
    }
}
