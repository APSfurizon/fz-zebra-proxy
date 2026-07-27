package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterIdentifier;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import net.furizon.zebra_proxy.features.printing.service.printers.PrinterService;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverConfig;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

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
    private final JobManagementService jobManagementService;


    public synchronized void invoke(@NotNull PrintIdContentPair pair, @NotNull QueuePair queue) {
        log.info("Printing {}", pair);

        byte[] pdfContent = exportToPdf(pair);
        if (pdfContent == null) {
            log.error("Failed to export pdf on job {}", pair);
            return;
        }
        PrinterIdentifier printer = printConfigService.getPrinterNamePerQueuePair(queue);
        if (printer == null) {
            log.error("Printer config not found for queue {}", queue);
            return;
        }
        jobManagementService.submitJob(printer, pair, pdfContent);
        jobManagementService.runAsync(printer);
    }



    private synchronized byte[] exportToPdf(@NotNull PrintIdContentPair pair) {
        Path tempHtml = null;
        long start = System.currentTimeMillis();
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
            log.debug("Exported pdf on job {} in {} ms", pair, System.currentTimeMillis() - start);
        }
    }
}
