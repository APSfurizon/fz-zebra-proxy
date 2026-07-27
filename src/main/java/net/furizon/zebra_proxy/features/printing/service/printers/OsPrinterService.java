package net.furizon.zebra_proxy.features.printing.service.printers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterIdentifier;
import net.furizon.zebra_proxy.infrastructure.pdfUtils.FzPDFPageable;
import net.furizon.zebra_proxy.infrastructure.pdfUtils.PrintingSettingsConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsPrinterService implements PrinterService {
    static OsPrinterService INSTANCE = null;

    @NotNull
    private final PrintingSettingsConfig printConfig;

    @Override
    public void printPdf(byte[] pdfContent, @NotNull PrintIdContentPair pair, @NotNull PrinterIdentifier settings) {
        try (PDDocument document = Loader.loadPDF(pdfContent)) {
            PrintService printer = findPrintService(settings);
            if (printer == null) {
                log.warn("Printer {} not found, using default printer", settings.getPrinterName());
                printer = findDefaultPrintService();
            }
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(generatePageable(document));
            job.setCopies(1);
            job.setJobName(String.format("fz-zebra-proxy (%s)", pair.getPrintId()));
            job.setPrintService(printer);
            job.print();

        } catch (IOException | PrinterException ex) {
            log.error("Exception while printing pdf on job {}", pair, ex);
        }
    }

    @Override
    public void queueDone(@NotNull PrinterIdentifier settings) {
    }
    @Override
    public void closeAll() {
    }

    private FzPDFPageable generatePageable(PDDocument document) {
        PrintingSettingsConfig.Card c = printConfig.getCard();
        double w = c.getWidth();
        double h = c.getHeight();
        boolean invertMediaOrientation = c.isInvertMediaOrientation();
        boolean invertPageFormatOrientation = c.isInvertPageformatOrientation();

        FzPDFPageable pageable = new FzPDFPageable(document);
        pageable.setPaperWidthIn(w);
        pageable.setPaperHeightIn(h);
        pageable.setImageableAreaXIn(0.0);
        pageable.setImageableAreaYIn(0.0);
        pageable.setImageableAreaWidthIn(w);
        pageable.setImageableAreaHeightIn(h);
        pageable.setInvertMediaOrientation(invertMediaOrientation);
        pageable.setInvertPageFormatOrientation(invertPageFormatOrientation);
        return pageable;
    }


    public @Nullable PrintService findDefaultPrintService() {
        return PrintServiceLookup.lookupDefaultPrintService();
    }

    public @Nullable PrintService findPrintService(@NotNull PrinterIdentifier printerIdentifier) {
        return findPrintService(printerIdentifier.getPrinterName());
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

    @Override
    public @NotNull Set<String> getAvailablePrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        return Arrays.stream(printServices).map(PrintService::getName).collect(Collectors.toUnmodifiableSet());
    }

    @PostConstruct
    private void init() {
        INSTANCE = this;
    }
}
