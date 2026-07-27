package net.furizon.zebra_proxy.features.printing.service.printers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.*;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.containers.internal.ImageAdjustmentLevels;
import com.zebra.sdk.common.card.graphics.enumerations.PrinterModel;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.settings.SettingsException;
import com.zebra.sdk.zmotif.job.ZebraCardJobSettingNamesZmotif;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import net.furizon.zebra_proxy.features.printing.dto.PrinterSettings;
import net.furizon.zebra_proxy.features.printing.dto.ZebraPrinterConfig;
import net.furizon.zebra_proxy.infrastructure.pdfUtils.PrintingSettingsConfig;
import net.furizon.zebra_proxy.infrastructure.zebraUtils.ZebraUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    private Map<String, Triple<Connection, ZebraCardPrinter, PrinterModel>> ipToOpenPrinters = new HashMap<>();
    private static final ReentrantLock MAP_MUTEX = new ReentrantLock(true);

    @NotNull
    private final PrintingSettingsConfig printConfig;

    @Override
    @SneakyThrows
    public void printPdf(byte[] pdfContent, @NotNull PrintIdContentPair pair, @NotNull PrinterSettings settings) {
        var p = getPrinter(settings);
        ZebraCardPrinter printer = p.getLeft();
        ZebraPrinterConfig config = p.getMiddle();
        PrinterModel printerModel = p.getRight();

        RenderingHints renderingHints = new RenderingHints(Map.of(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        ));
        ImageAdjustmentLevels imgAdjLevels = config.getImgageAdjustmentLevels();

        long startProcess = System.currentTimeMillis();
        try (PDDocument document = Loader.loadPDF(pdfContent)) {
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(false);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                long startRender = System.currentTimeMillis();

                BufferedImage image = renderPageToCard(renderer, page, renderingHints);

                int wPx = image.getWidth();
                int hPx = image.getHeight();
                OrientationType orientation = wPx > hPx ? OrientationType.Landscape : OrientationType.Portrait;
                applySettings(printer, config, orientation); //Settings are not persistent
                var panel = List.of(panel(
                        CardSide.Front,
                        config,
                        ZebraUtils.convertToimage(
                                image,
                                imgAdjLevels,
                                printerModel, orientation,
                                config.getPrintType(), config.getMonochromeConversion(),
                                renderingHints,
                                config.getColorProfile()
                        )
                ));
                log.debug("Rendered page {}/{} of {} in {} ms", page, document.getNumberOfPages(), pair.getPrintId(), System.currentTimeMillis() - startRender);

                Integer jobId = null;
                try {
                    jobId = print(printer, panel);
                } catch (ConnectionException e) {
                    log.error("Failed to print page {}/{} of {} to printer {}", page, document.getNumberOfPages(), pair.getPrintId(), settings.getPrinterName(), e);
                    queueDone(settings);
                    printer = getPrinter(settings).getLeft();
                    applySettings(printer, config, orientation);
                    jobId = print(printer, panel);
                }
                long startPage = System.currentTimeMillis();
                if (jobId != null) waitForJob(printer, jobId, pair);
                log.debug("Printed page {}/{} of {} in {} ms", page, document.getNumberOfPages(), pair.getPrintId(), System.currentTimeMillis() - startPage);
            }
        }
        log.debug("Printed {} in {} ms", pair.getPrintId(), System.currentTimeMillis() - startProcess);
    }

    private int print(@NotNull ZebraCardPrinter printer, @NotNull List<GraphicsInfo> panels) throws SettingsException, ConnectionException, ZebraCardException {
        long start = System.currentTimeMillis();
        int jobId = printer.print(1, panels);
        log.debug("Launched print in {} ms", System.currentTimeMillis() - start);
        return jobId;
    }

    private static String waitForJob(ZebraCardPrinter printer, int jobId, @NotNull PrintIdContentPair pair) throws Exception {
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

            log.debug("Job {} ({}): {} / {} (Error? Code:{} - Text: {})", jobId, pair.getPrintId(), status, position, errorCode, errorText);

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

    private @NotNull GraphicsInfo panel(@NotNull CardSide side, @NotNull ZebraPrinterConfig config, @NotNull ZebraCardImageI image) {
        GraphicsInfo info = new GraphicsInfo();
        info.side = side;
        info.printType = config.getPrintType();
        info.graphicType = GraphicType.BMP;
        info.graphicData = image;
        info.xOffset = 0;
        info.yOffset = 0;
        info.fillColor = -1;
        info.opacity = 0;
        info.overprint = false;
        info.preheat = config.getColorPreheat();
        return info;
    }
    private void applySettings(ZebraCardPrinter printer, @NotNull ZebraPrinterConfig config, @NotNull OrientationType orientation) {
        setJobSetting(printer, ZebraCardJobSettingNames.CARD_SOURCE, "Feeder");
        setJobSetting(printer, ZebraCardJobSettingNames.CARD_DESTINATION, "Eject");
        setJobSetting(printer, ZebraCardJobSettingNames.K_OPTIMIZATION_FRONT, "Mixed");

        // Don't leave card images sitting on the printer after the job.
        setJobSetting(printer, ZebraCardJobSettingNames.DELETE_AFTER, "yes");

        //setJobSetting(printer, ZebraCardJobSettingNamesZmotif.CARD_THICKNESS, String.valueOf(CARD_THICKNESS_MILS));

        setJobSetting(printer, ZebraCardJobSettingNamesZmotif.COLOR_PREHEAT, String.valueOf(config.getColorPreheat()));
        setJobSetting(printer, ZebraCardJobSettingNamesZmotif.K_PREHEAT_FRONT, String.valueOf(config.getKPreheat()));
        setJobSetting(printer, ZebraCardJobSettingNamesZmotif.SHARPENING_FRONT, config.getSharpeningLevel().name());

        setJobSetting(printer, ZebraCardJobSettingNamesZmotif.ORIENTATION_FRONT, orientation.name());
    }
    /** Unknown keys are a firmware difference, not a reason to fail the job. */
    private void setJobSetting(@NotNull ZebraCardPrinter printer, @NotNull String name, @NotNull String value) {
        try {
            printer.setJobSetting(name, value);
        } catch (Exception e) {
            log.error("Warning: job setting {}={} rejected - {}", name, value, e.getMessage());
        }
    }


    private BufferedImage renderPageToCard(@NotNull PDFRenderer renderer, int pageIndex, @Nullable RenderingHints renderingHints) throws IOException {
        var conf = printConfig.getCard();
        double dpi = conf.getDpi();

        BufferedImage rendered = renderer.renderImageWithDPI(pageIndex, (float) (dpi * conf.getSupersampling()), ImageType.RGB);

        int targetW = conf.widthPx(dpi);
        int targetH = conf.heightPx(dpi);
        BufferedImage resized = new BufferedImage(targetW, targetH, rendered.getType());
        Graphics2D g = resized.createGraphics();

        g.setRenderingHints(renderingHints);

        //Match the orientation
        boolean isTargetHorizontal = targetW > targetH;
        boolean isSourceHorizontal = rendered.getWidth() > rendered.getHeight();

        if (isSourceHorizontal != isTargetHorizontal) {
            g.translate(targetW, 0);
            g.rotate(Math.PI / 2.0);

            int swap = targetW;
            targetW = targetH;
            targetH = swap;
        }

        g.drawImage(rendered, 0, 0, targetW, targetH, null);
        g.dispose();

        return resized;
    }

    @Override
    public void queueDone(@NotNull PrinterSettings settings) {
        try {
            MAP_MUTEX.lock();
            ZebraPrinterConfig z = printerNameToFullConfig.get(settings.getPrinterName());
            String ip = z.getIp();
            Triple<Connection, ZebraCardPrinter, PrinterModel> p = ipToOpenPrinters.get(ip);
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

    private @NotNull Triple<ZebraCardPrinter, ZebraPrinterConfig, PrinterModel> getPrinter(@NotNull PrinterSettings settings) throws ConnectionException, SettingsException, ZebraCardException {
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
            var triple = ipToOpenPrinters.get(config.getIp());
            if (triple == null) {
                triple = open(config);
            }
            return Triple.of(triple.getMiddle(), config, triple.getRight());
        } finally {
            MAP_MUTEX.unlock();
        }
    }

    private void close(@NotNull Triple<Connection, ZebraCardPrinter, PrinterModel> triple, @NotNull String ip) {
        try {
            MAP_MUTEX.lock();
            try {
                triple.getMiddle().destroy();
            } catch (ZebraCardException e) {
                e.printStackTrace();
            }
            try {
                triple.getLeft().close();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
            ipToOpenPrinters.remove(ip);
        } finally {
            MAP_MUTEX.unlock();
        }
    }
    private @NotNull Triple<Connection, ZebraCardPrinter, PrinterModel> open(@NotNull ZebraPrinterConfig config) throws ConnectionException, SettingsException, ZebraCardException {
        try {
            MAP_MUTEX.lock();
            long start = System.currentTimeMillis();
            Connection connection = new TcpConnection(config.getIp(), 9100);
            connection.open();
            ZebraCardPrinter printer = ZebraCardPrinterFactory.getInstance(connection);
            PrinterModel printerModel = Objects.requireNonNull(ZebraUtils.getPrinterModel(printer));
            var triple = Triple.of(connection, printer, printerModel);
            ipToOpenPrinters.put(config.getIp(), triple);
            log.debug("Opened printer {} in {} ms", config.getName(), System.currentTimeMillis() - start);
            return triple;
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
