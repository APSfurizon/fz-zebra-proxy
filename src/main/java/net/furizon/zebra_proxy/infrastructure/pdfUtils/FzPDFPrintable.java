package net.furizon.zebra_proxy.infrastructure.pdfUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.io.IOException;

@Slf4j
@Getter
public class FzPDFPrintable implements Printable {
    private final PDPageTree pageTree;
    private final PDFRenderer renderer;

    //@Setter
    //private boolean center = true;

    public FzPDFPrintable(@NotNull PDDocument document) {
        renderer = new PDFRenderer(document);
        pageTree = document.getPages();
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex < 0 || pageIndex >= pageTree.getCount()) {
            return NO_SUCH_PAGE;
        }
        try {
            Graphics2D graphics2D = (Graphics2D)graphics;

            PDPage page = pageTree.get(pageIndex);
            PDRectangle cropBox = FzPDFUtils.getRotatedCropBox(page);

            log.debug("Printing page {} with crop box {} ({}x{}). ImgW = {}, ImgH = {} ImgX = {}, ImgY = {}",
                    pageIndex,
                    cropBox, cropBox.getWidth(), cropBox.getHeight(),
                    pageFormat.getImageableWidth(), pageFormat.getImageableHeight(),
                    pageFormat.getImageableX(), pageFormat.getImageableY()
            );

            // the imageable area is the area within the page margins
            final double imageableWidth = pageFormat.getImageableWidth();
            final double imageableHeight = pageFormat.getImageableHeight();

            // scale to fit
            double scaleX = imageableWidth / cropBox.getWidth();
            double scaleY = imageableHeight / cropBox.getHeight();

            log.debug("scaleX = {}, scaleY = {}", scaleX, scaleY);

            // set the graphics origin to the origin of the imageable area (i.e the margins)
            graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // No need to center if we're already scaling in both directions
            // center on page
            //if (center) {
            //    double dx = (imageableWidth - cropBox.getWidth() * scaleX) / 2.0;
            //    double dy = (imageableHeight - cropBox.getHeight() * scaleY) / 2.0;
            //    if (dx >= 0.0 && dy >= 0.0) {
            //        graphics2D.translate(dx, dy);
            //    } else {
            //        // PDFBOX-3117 and https://lists.apache.org/thread/12s9tc93ofgmjfq1dpqfps9p725l0wwr
            //        log.warn("Centering disabled because of negative translation value ({}, {})", dx, dy);
            //    }
            //}

            // draw to graphics using PDFRender
            graphics2D.setBackground(Color.WHITE);
            renderer.setSubsamplingAllowed(false);
            renderer.setRenderingHints(null);
            renderer.renderPageToGraphics(pageIndex, graphics2D, (float) scaleX, (float) scaleY, RenderDestination.PRINT);


            return PAGE_EXISTS;
        }
        catch (IOException e)
        {
            throw new PrinterIOException(e);
        }
    }
}
