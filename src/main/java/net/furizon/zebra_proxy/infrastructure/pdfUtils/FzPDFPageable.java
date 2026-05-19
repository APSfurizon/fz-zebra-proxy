package net.furizon.zebra_proxy.infrastructure.pdfUtils;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.jetbrains.annotations.Nullable;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;

@Slf4j
@Getter
@ToString
public class FzPDFPageable extends Book {
    @NotNull
    @ToString.Exclude
    private final PDDocument document;
    private final int numberOfPages;

    @Setter private boolean invertMediaOrientation = false;
    @Setter private boolean invertPageFormatOrientation = false;
    @Nullable @Setter private Double paperWidthIn = null;
    @Nullable @Setter private Double paperHeightIn = null;
    @Nullable @Setter private Double imageableAreaXIn = null;
    @Nullable @Setter private Double imageableAreaYIn = null;
    @Nullable @Setter private Double imageableAreaWidthIn = null;
    @Nullable @Setter private Double imageableAreaHeightIn = null;

    public FzPDFPageable(@NotNull PDDocument document) {
        this.document = document;
        numberOfPages = document.getNumberOfPages();
    }

    @Override
    public int getNumberOfPages() {
        return numberOfPages;
    }

    @Override
    public PageFormat getPageFormat(int pageIndex) {
        PDPage page = document.getPage(pageIndex);
        PDRectangle mediaBox = FzPDFUtils.getRotatedMediaBox(page);
        PDRectangle cropBox = FzPDFUtils.getRotatedCropBox(page);

        double mediaW = paperWidthIn == null ? mediaBox.getWidth() : paperWidthIn;
        double mediaH = paperHeightIn == null ? mediaBox.getHeight() : paperHeightIn;
        double imgX = imageableAreaXIn == null ? cropBox.getLowerLeftX() : imageableAreaXIn * 72.0;
        double imgY = imageableAreaYIn == null ? cropBox.getLowerLeftY() : imageableAreaYIn * 72.0;
        double imgW = imageableAreaWidthIn == null ? cropBox.getWidth() : imageableAreaWidthIn * 72.0;
        double imgH = imageableAreaHeightIn == null ? cropBox.getHeight() : imageableAreaHeightIn * 72.0;

        log.debug("Called with params: {}", toString());
        log.debug("mediaBoxW = {}, mediaBoxH = {}, cropLLx = {}, cropLLy = {}, cropW = {}, cropH = {}",
                mediaBox.getWidth(), mediaBox.getHeight(),
                cropBox.getLowerLeftX(), cropBox.getLowerLeftY(),
                cropBox.getWidth(), cropBox.getHeight()
        );


        // Java does not seem to understand landscape paper sizes, i.e. where width > height, it
        // always crops the imageable area as if the page were in portrait. I suspect that this is
        // a JDK bug but it might be by design, see PDFBOX-2922.
        //
        // As a workaround, we normalise all Page(s) to be portrait, then flag them as landscape in
        // the PageFormat.
        Paper paper;
        boolean isLandscape;
        if ((mediaBox.getWidth() > mediaBox.getHeight()) ^ invertMediaOrientation) {
        //if ((mediaW > mediaH) ^ invertOrientation) {
            // rotate
            paper = new Paper();
            paper.setSize(mediaH, mediaW);
            paper.setImageableArea(imgY, imgX, imgH, imgW);
            isLandscape = true;
        } else {
            paper = new Paper();
            paper.setSize(mediaW, mediaH);
            paper.setImageableArea(imgX, imgY, imgW, imgH);
            isLandscape = false;
        }

        log.debug("isLandscape={}; mediaW={}; mediaH={}; imgX={}; imgY={}; imgW={}; imgH={}",
                isLandscape,
                mediaW, mediaH,
                imgX, imgY,
                imgW, imgH
        );


        PageFormat format = new PageFormat();
        format.setPaper(paper);

        // auto portrait/landscape
        format.setOrientation((isLandscape ^ invertPageFormatOrientation) ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);

        return format;
    }

    @Override
    public Printable getPrintable(int i) {
        if (i >= numberOfPages) {
            throw new IndexOutOfBoundsException(i + " >= " + numberOfPages);
        }
        return new FzPDFPrintable(document);
    }
}
