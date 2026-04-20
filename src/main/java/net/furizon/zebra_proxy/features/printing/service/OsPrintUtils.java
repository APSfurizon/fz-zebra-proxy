package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsPrintUtils {


    public @Nullable PrintService findDefaultPrintService() {
        return PrintServiceLookup.lookupDefaultPrintService();
    }

    public @Nullable PrintService findPrintService(@NotNull PrinterSettings printerSettings) {
        return findPrintService(printerSettings.getPrinterName());
    }
    public @Nullable PrintService findPrintService(@NotNull String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        return null;
    }

    public @NotNull Set<String> getAvailablePrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        return Arrays.stream(printServices).map(PrintService::getName).collect(Collectors.toUnmodifiableSet());
    }
}
