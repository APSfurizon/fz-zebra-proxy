package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverConfig;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverConfiguration;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintingService {
    @NotNull
    private final ChromeDriver webDriver;
    @NotNull
    private final PrintConfigService printConfigService;
    @NotNull
    private final WebdriverConfig webdriverConfig;
    @NotNull
    private final OsPrintUtils printUtils;


    public synchronized void invoke(@NotNull PrintIdContentPair pair, @NotNull QueuePair queue) {
        log.info("Printing {}", pair);

        byte[] pdfContent = exportToPdf(pair);
        if (pdfContent == null) {
            log.error("Failed to export pdf on job {}", pair);
            return;
        }
        PrinterSettings printerConfig = printConfigService.getPrinterNamePerQueuePair(queue);
        if (printerConfig == null) {
            log.error("Printer config not found for queue {}", queue);
            return;
        }
        printPdf(pdfContent, pair, printerConfig);
        log.debug("Printed {}", pair);
    }

    private void printPdf(byte[] pdfContent, @NotNull PrintIdContentPair pair, @NotNull PrinterSettings settings) {
        try (PDDocument document = Loader.loadPDF(pdfContent)) {

            PrintService printer = printUtils.findPrintService(settings);
            if (printer == null) {
                log.warn("Printer {} not found, using default printer", settings.getPrinterName());
                printer = printUtils.findDefaultPrintService();
            }
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            job.setPrintService(printer);
            job.print();

        } catch (IOException | PrinterException ex) {
            log.error("Exception while printing pdf on job {}", pair, ex);
        }
    }

    private byte[] exportToPdf(@NotNull PrintIdContentPair pair) {
        Path tempHtml = null;
        try {
            tempHtml = Files.createTempFile(null, ".html");
            Files.write(tempHtml, pair.getHtml().getBytes());

            webDriver.get(tempHtml.toAbsolutePath().toString());
            WebdriverUtils.waitForPageLoad(webDriver, webdriverConfig.getLoadTimeout());

            //Cannot use the standard print otherwise it would print on A4
            Map<String, Object> printParams = new HashMap<>();
            printParams.put("printBackground", true);
            printParams.put("preferCSSPageSize", true);
            printParams.put("marginTop", 0);
            printParams.put("marginBottom", 0);
            printParams.put("marginLeft", 0);
            printParams.put("marginRight", 0);
            Map<String, Object> result = webDriver.executeCdpCommand("Page.printToPDF", printParams);

            byte[] pdfContent = Base64.getDecoder().decode((String) result.get("data"));
            Files.write(Paths.get("output.pdf"), pdfContent);
            return pdfContent;

        } catch (IOException e) {
            log.error("IOException while exporting pdf on job {}", pair, e);
            return null;
        } finally {
            if (tempHtml != null) {
                try {
                    Files.deleteIfExists(tempHtml);
                } catch (IOException e) {
                    log.error("IOException while deleting temp file {} for job {}", tempHtml, pair, e);
                }
            }
        }
    }
}
