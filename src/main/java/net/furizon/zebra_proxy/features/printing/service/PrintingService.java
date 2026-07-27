package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterIdentifier;
import net.furizon.zebra_proxy.features.printing.dto.QueuePair;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverConfig;
import net.furizon.zebra_proxy.infrastructure.selenium.WebdriverUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
        int retries = 0;
        while(true) {
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
                if(!isPdfValid(pdfContent) && ++retries <= 3) {
                    log.error("INVALID PDF DETECTED on job {}, retrying", pair);
                    continue;
                }
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

    private boolean isPdfValid(byte[] pdf) {
        //Heuristic to understand when a PDF is invalid, IE when the background hasn't rendered correctly:
        //We downsample the first page of the pdf, divide in in 4 quadrants and take an average of them.
        //If the average is "too white" for at least two quadrants, we consider the pdf wrong
        try (PDDocument document = Loader.loadPDF(pdf)) {
            if (document.getNumberOfPages() <= 0) {
                return false;
            }
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(true); //Gotta go fast
            BufferedImage rendered = renderer.renderImageWithDPI(0, 50.0f, ImageType.RGB);
            int w = rendered.getWidth() / 2;
            int h = rendered.getHeight() / 2;

            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            //ImageIO.write(rendered, "bmp", out);
            //Files.write(Path.of("data/shitLow.bmp"), out.toByteArray());

            int emptySections = 0;

            if (isAvgWhite(getAvarageOfSection(0, 0, w, h, rendered))) {
                emptySections++;
            }
            if (isAvgWhite(getAvarageOfSection(w, 0, w, h, rendered))) {
                emptySections++;
                if (emptySections >= 2) return false;
            }
            if (isAvgWhite(getAvarageOfSection(0, h, w, h, rendered))) {
                emptySections++;
                if (emptySections >= 2) return false;
            }
            if (isAvgWhite(getAvarageOfSection(w, h, w, h, rendered))) {
                emptySections++;
                if (emptySections >= 2) return false;
            }

        } catch (IOException e) {
            log.error("Failed to load pdf", e);
            return false;
        }
        return true;
    }

    private boolean isAvgWhite(@NotNull Triple<Long, Long, Long> avg) {
        final long threshold = 220L;
        log.debug("Average {}", avg);
        return avg.getLeft() > threshold && avg.getMiddle() > threshold && avg.getRight() > threshold;
    }

    private @NotNull Triple<Long, Long, Long> getAvarageOfSection(int x, int y, int w, int h, @NotNull BufferedImage img) {
        int[] data = img.getRGB(x, y, Math.min(w, img.getWidth() - x), Math.min(h, img.getHeight() - h), null, 0, w);
        long r = 0L, g = 0L, b = 0L;
        for (int rgb : data) {
            r += (long) ((rgb >> 16) & 0xFF);
            g += (long) ((rgb >> 8) & 0xFF);
            b += (long) (rgb & 0xFF);
        }
        long len = data.length;
        return Triple.of(r / len, g / len, b / len);
    }
}
