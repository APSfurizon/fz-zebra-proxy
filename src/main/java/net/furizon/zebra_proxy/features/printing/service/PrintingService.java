package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverConfig;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            log.info("Document {}", document);

            PrintService printer = printUtils.findPrintService(settings);
            if (printer == null) {
                log.warn("Printer {} not found, using default printer", settings.getPrinterName());
                printer = printUtils.findDefaultPrintService();
            }
            PrinterJob job = PrinterJob.getPrinterJob();
            //job.setPageable(new PDFPageable(document));
            job.setPrintable(new PDFPrintable(document, Scaling.STRETCH_TO_FIT), generatePageFormat());
            job.setCopies(1);
            job.setJobName(String.format("fz-zebra-proxy (%s)", pair.getPrintId()));
            job.setPrintService(printer);
            job.print();

        } catch (IOException | PrinterException ex) {
            log.error("Exception while printing pdf on job {}", pair, ex);
        }
    }

    private PageFormat generatePageFormat() {
        log.debug("Test #2");
        double cardWidthPoints = 2.375 * 72.0;
        double cardHeightPoints = 1.125 * 72.0;

        Paper cardPaper = new Paper();
        cardPaper.setSize(cardWidthPoints, cardHeightPoints);
        cardPaper.setImageableArea(0.0, 0.0, cardWidthPoints, cardHeightPoints);

        log.info("PAPER {}", cardPaper);

        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(cardPaper);
        //pageFormat.setOrientation(PageFormat.LANDSCAPE);
        log.info("FORMAT {}", pageFormat);
        return pageFormat;
    }

    private byte[] exportToPdf(@NotNull PrintIdContentPair pair) {
        Path tempHtml = null;
        try {
            tempHtml = Files.createTempFile(null, ".html");
            log.debug("Writing temp html to {}", tempHtml);
            Files.write(tempHtml, pair.getHtml().getBytes());

            webDriver.get(tempHtml.toAbsolutePath().toString());
            WebdriverUtils.waitForPageLoad(webDriver, webdriverConfig.getLoadTimeout(), webdriverConfig.getExtraWaitMs());

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
            //Files.write(Paths.get("output.pdf"), pdfContent);
            return pdfContent;

        } catch (IOException e) {
            log.error("IOException while exporting pdf on job {}", pair, e);
            return null;
        } finally {
            if (tempHtml != null) {
                try {
                    log.debug("Deleting temp html from {}", tempHtml);
                    Files.deleteIfExists(tempHtml);
                } catch (IOException e) {
                    log.error("IOException while deleting temp file {} for job {}", tempHtml, pair, e);
                }
            }
        }
    }
}
