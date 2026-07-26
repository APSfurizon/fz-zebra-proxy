package net.furizon.zebra_proxy.features.printing.service.printers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.CardSide;
import com.zebra.sdk.common.card.enumerations.GraphicType;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.graphics.ZebraCardGraphics;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.ZebraGraphics;
import com.zebra.sdk.common.card.graphics.enumerations.RotationType;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.zmotif.job.ZebraCardJobSettingNamesZmotif;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.ZebraPrinterConfig;
import net.furizon.zebra_proxy.infrastructure.pdfUtils.PrintingSettingsConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZebraService implements PrinterService {
    static ZebraService INSTANCE = null;
    public static final String ZEBRA_PRINT_NAME_PREPEND = "(REMOTE ZEBRA) ";
    private static final Path CONFIG_FILE = Paths.get("data/zebraPrinters.json");
    private static final long JOB_TIMEOUT_MS = 20_000L;   // whole job
    private static final long FEED_TIMEOUT_MS = 10_000L;   // stuck waiting for a card
    private static final long POLL_INTERVAL_MS = 1_000L;
    @NotNull
    private final ObjectMapper objectMapper;

    private Map<String, ZebraPrinterConfig> printerNameToFullConfig = Collections.emptyMap();
    private Map<String, Pair<Connection, ZebraCardPrinter>> ipToOpenPrinters = new HashMap<>();
    private static final ReentrantLock MAP_MUTEX = new ReentrantLock(true);

    @NotNull
    private final PrintingSettingsConfig printConfig;

    @Override
    @SneakyThrows
    public void printPdf(byte[] pdfContent, @NotNull PrintIdContentPair pair, @NotNull PrinterSettings settings) {
        //Files.write(Paths.get("data/" + pair.getPrintId() + ".pdf"), pdfContent);
        ZebraCardPrinter printer = getPrinter(settings);
        ZebraGraphics graphics = new ZebraCardGraphics(printer);
        try (PDDocument document = Loader.loadPDF(pdfContent)) {
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(false);
            List<GraphicsInfo> panels = new ArrayList<>(document.getNumberOfPages());
            for (int page = 0; page < document.getNumberOfPages(); page++) {

                BufferedImage image = renderPageToCard(renderer, document.getPage(page), page);
                byte[] imageBytes = toBmp(image);

                int wPx = image.getWidth();
                int hPx = image.getHeight();
                if (wPx > hPx) {
                    setJobSetting(printer, ZebraCardJobSettingNamesZmotif.ORIENTATION_FRONT, "Landscape");
                } else {
                    setJobSetting(printer, ZebraCardJobSettingNamesZmotif.ORIENTATION_FRONT, "Portrait");
                }
                panels.add(panel(CardSide.Front, PrintType.Color, render(graphics, PrintType.Color, imageBytes, wPx, hPx)));
                graphics.clear();

                int jobId = printer.print(1, panels);
                waitForJob(printer, jobId);
            }
            graphics.close();
        }
    }

    private static String waitForJob(ZebraCardPrinter printer, int jobId) throws Exception {
        long feedingSince = System.currentTimeMillis();
        long hardDeadline = feedingSince + JOB_TIMEOUT_MS;
        boolean wasFeeding = false;
        boolean cancelled = false;

        while (true) {
            JobStatusInfo job = printer.getJobStatus(jobId);

            String status = job.printStatus == null ? "" : job.printStatus;
            String position = job.cardPosition == null ? "" : job.cardPosition;
            int errorCode = job.errorInfo != null ? job.errorInfo.value : 0;
            String errorText = job.errorInfo != null ? job.errorInfo.description : "";

            log.debug("Job {}: {} / {} (Error? Code:{} - Text: {})", jobId, status, position, errorCode, errorText);

            if (cancelled && errorCode > 0) {
                return null;
            }

            if (status.contains("done_ok")) {
                return status;
            }
            if (status.contains("error") || status.contains("cancelled")) {
                log.error("Job {} ended as '{}' with error code {} and text '{}'", jobId, status, errorCode, errorText);
            }

            // A reported error won't clear itself: cancel and let the loop see the result.
            if (errorCode > 0 && !cancelled) {
                printer.cancel(jobId);
                cancelled = true;
            }

            // Stuck in "feeding" usually means an empty hopper.
            boolean feeding = position.contains("feeding");
            if (feeding && !wasFeeding) {
                feedingSince = System.currentTimeMillis();
            }
            wasFeeding = feeding;

            if (feeding && !cancelled && System.currentTimeMillis() - feedingSince > FEED_TIMEOUT_MS) {
                log.error("No card arrived, cancelling job {}", jobId);
                printer.cancel(jobId);
                cancelled = true;
            }

            if (System.currentTimeMillis() > hardDeadline) {
                if (!cancelled) {
                    printer.cancel(jobId);
                    cancelled = true;
                }
                log.error("Job {} did not finish within " + JOB_TIMEOUT_MS + " ms", jobId);
            }

            Thread.sleep(POLL_INTERVAL_MS);
        }
    }
    private ZebraCardImageI render(ZebraGraphics graphics, PrintType printType, byte[] imageData, int wPx, int hPx) throws Exception {
        boolean isPortrait = wPx < hPx;
        graphics.initialize(wPx, hPx, isPortrait ? OrientationType.Portrait : OrientationType.Landscape, printType, Color.WHITE);
        graphics.drawImage(imageData, 0, 0, 0, 0, !isPortrait ? RotationType.Rotate90FlipNone : RotationType.RotateNoneFlipNone);
        return graphics.createImage();
    }
    private GraphicsInfo panel(CardSide side, PrintType printType, ZebraCardImageI image) {
        try {
            Files.write(Paths.get("data/shit.bmp"), image.getImageData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GraphicsInfo info = new GraphicsInfo();
        info.side = side;
        info.printType = printType;
        info.graphicType = GraphicType.BMP;
        info.graphicData = image;
        info.xOffset = 0;
        info.yOffset = 0;
        info.fillColor = -1;
        info.opacity = 0;
        info.overprint = false;
        return info;
    }
    private void applySettings(ZebraCardPrinter printer) {
        setJobSetting(printer, ZebraCardJobSettingNames.CARD_SOURCE, "Feeder");
        setJobSetting(printer, ZebraCardJobSettingNames.CARD_DESTINATION, "Eject");
        setJobSetting(printer, ZebraCardJobSettingNames.K_OPTIMIZATION_FRONT, "Mixed");

        // Don't leave card images sitting on the printer after the job.
        setJobSetting(printer, ZebraCardJobSettingNames.DELETE_AFTER, "yes");

        //setJobSetting(printer, ZebraCardJobSettingNamesZmotif.CARD_THICKNESS, String.valueOf(CARD_THICKNESS_MILS));

        setJobSetting(printer, ZebraCardJobSettingNamesZmotif.COLOR_PREHEAT, "0");
        setJobSetting(printer, ZebraCardJobSettingNamesZmotif.K_PREHEAT_FRONT, "0");
    }
    /** Unknown keys are a firmware difference, not a reason to fail the job. */
    private void setJobSetting(@NotNull ZebraCardPrinter printer, @NotNull String name, @NotNull String value) {
        try {
            printer.setJobSetting(name, value);
        } catch (Exception e) {
            System.err.println("Warning: job setting " + name + "=" + value + " rejected - " + e.getMessage());
        }
    }
    /** First job setting key containing all the given fragments, or null if there isn't one. */
    private String findJobSettingKey(@NotNull ZebraCardPrinter printer, String... mustContain) {
        try {
            for (String key : printer.getJobSettings()) {
                String lower = key.toLowerCase();
                boolean matches = true;
                for (String fragment : mustContain) {
                    if (!lower.contains(fragment)) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return key;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not read job setting names - " + e.getMessage());
        }
        return null;
    }

    /**
     * Rasterises one page straight to card size.
     *
     * The scale is worked out from the page's CropBox up front, so PDFBox renders once at the
     * final resolution instead of rendering at a fixed DPI and being resampled afterwards.
     * Text and vector art are rasterised directly at their output size, and embedded photos go
     * through one resampling step instead of two.
     */
    private BufferedImage renderPageToCard(@NotNull PDFRenderer renderer, @NotNull PDPage page, int pageIndex) throws IOException {

        // Visible area in PDF user space units (1/72 in). PDFBox applies /Rotate during
        // rendering, so predict the output size against the rotated box.
        PDRectangle crop = page.getCropBox();
        float widthPt = crop.getWidth();
        float heightPt = crop.getHeight();
        int rotation = page.getRotation();
        if (rotation == 90 || rotation == 270) {
            float swap = widthPt;
            widthPt = heightPt;
            heightPt = swap;
        }

        // A portrait page gets turned onto the landscape card, so measure post-rotation.
        boolean turnSideways = widthPt > heightPt;
        float fitWidthPt = turnSideways ? heightPt : widthPt;
        float fitHeightPt = turnSideways ? widthPt : heightPt;

        var conf = printConfig.getCard();
        double dpi = conf.getDpi();
        float scale = Math.min(conf.widthPx(dpi) / fitWidthPt, conf.heightPx(dpi) / fitHeightPt);

        BufferedImage rendered = renderer.renderImage(pageIndex, scale, ImageType.RGB);
        //if (turnSideways) {
        //    BufferedImage out = new BufferedImage(rendered.getHeight(), rendered.getWidth(), BufferedImage.TYPE_INT_RGB);
        //    Graphics2D g = out.createGraphics();
        //    try {
        //        g.translate(rendered.getHeight(), 0);
        //        g.rotate(Math.PI / 2.0);
        //        g.drawImage(rendered, 0, 0, null);
        //        rendered = out;
        //    } finally {
        //        g.dispose();
        //    }
        //}
        return rendered;
    }
    private byte[] toBmp(@NotNull BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", out);
        return out.toByteArray();
    }

    @Override
    public void queueDone(@NotNull PrinterSettings settings) {
        try {
            MAP_MUTEX.lock();
            ZebraPrinterConfig z = printerNameToFullConfig.get(settings.getPrinterName());
            String ip = z.getIp();
            Pair<Connection, ZebraCardPrinter> p = ipToOpenPrinters.get(ip);
            close(p, ip);
        } finally {
            MAP_MUTEX.unlock();
        }
    }

    @Override
    public void closeAll() {
        try {
            MAP_MUTEX.lock();
            ipToOpenPrinters.forEach((ip, pair) -> close(pair, ip));
        } finally {
            MAP_MUTEX.unlock();
        }
    }

    private @NotNull ZebraCardPrinter getPrinter(@NotNull PrinterSettings settings) throws ConnectionException {
        try {
            MAP_MUTEX.lock();
            String name = settings.getPrinterName();
            if (name.startsWith(ZEBRA_PRINT_NAME_PREPEND)) {
                name = name.substring(ZEBRA_PRINT_NAME_PREPEND.length());
            }
            ZebraPrinterConfig config = printerNameToFullConfig.get(name);
            if (config == null) {
                throw new RuntimeException("No printer found with name '" + name + "'");
            }
            var pair = ipToOpenPrinters.get(config.getIp());
            if (pair != null) {
                return pair.getRight();
            }
            return open(config);
        } finally {
            MAP_MUTEX.unlock();
        }
    }

    private void close(@NotNull Pair<Connection, ZebraCardPrinter> pair, @NotNull String ip) {
        try {
            MAP_MUTEX.lock();
            try {
                pair.getRight().destroy();
            } catch (ZebraCardException e) {
                e.printStackTrace();
            }
            try {
                pair.getLeft().close();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
            ipToOpenPrinters.remove(ip);
        } finally {
            MAP_MUTEX.unlock();
        }
    }
    private @NotNull ZebraCardPrinter open(@NotNull ZebraPrinterConfig config) throws ConnectionException {
        try {
            MAP_MUTEX.lock();
            Connection connection = new TcpConnection(config.getIp(), 9100);
            connection.open();
            ZebraCardPrinter printer = ZebraCardPrinterFactory.getInstance(connection);
            applySettings(printer);
            ipToOpenPrinters.put(config.getIp(), Pair.of(connection, printer));
            return printer;
        } finally {
            MAP_MUTEX.unlock();
        }
    }

    public @NotNull Set<String> getAvailablePrinters() {
        return printerNameToFullConfig.keySet();
    }

    @PostConstruct
    private void init() {
        INSTANCE = this;
        try {
            MAP_MUTEX.lock();
            if (!Files.exists(CONFIG_FILE)) {
                log.error("Zebra printers file not found");
                return;
            }
            List<ZebraPrinterConfig> l = objectMapper.readValue(Files.readString(CONFIG_FILE), new TypeReference<List<ZebraPrinterConfig>>() {});
            printerNameToFullConfig = l.stream().collect(Collectors.toMap(ZebraPrinterConfig::getName, p -> p));
            log.info("Loaded {} zebra printers", printerNameToFullConfig.size());
        } catch (JsonProcessingException e) {
            log.error("Invalid json while loading zebra printers from disk", e);
            //throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Failed to read zebra printers from disk", e);
            //throw new RuntimeException(e);
        } finally {
            MAP_MUTEX.unlock();
        }
    }
}
