package net.furizon.zebra_proxy.features.printing.service.printers;

import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface PrinterService {
    @NotNull Set<String> getAvailablePrinters();

    void printPdf(byte[] pdfContent, @NotNull PrintIdContentPair pair, @NotNull PrinterSettings settings);

    void queueDone(@NotNull PrinterSettings settings);

    void closeAll();

    static @NotNull List<PrinterService> getAllPrinterServices() {
        return List.of(OsPrinterService.INSTANCE, ZebraService.INSTANCE);
    }
    static @NotNull List<String> getAllAvailablePrinters() {
        List<String> printers = new ArrayList<>();
        printers.addAll(OsPrinterService.INSTANCE.getAvailablePrinters());
        ZebraService.INSTANCE.getAvailablePrinters().forEach(s -> printers.add(ZebraService.ZEBRA_PRINT_NAME_PREPEND + s));
        return printers;
    }

    static @NotNull PrinterService getPrinterService(@NotNull String printerName){
        if (printerName.startsWith(ZebraService.ZEBRA_PRINT_NAME_PREPEND)) {
            return ZebraService.INSTANCE;
        }
        return OsPrinterService.INSTANCE;
    }
}
