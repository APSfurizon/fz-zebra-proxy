package net.furizon.zebra_proxy.infrastructure.pdfUtils;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class FzPDFUtils {
    /**
     * This will find the MediaBox with rotation applied, for this page by looking up the hierarchy
     * until it finds them.
     *
     * @return The MediaBox at this level in the hierarchy.
     */
    public static PDRectangle getRotatedMediaBox(PDPage page)
    {
        PDRectangle mediaBox = page.getMediaBox();
        int rotationAngle = page.getRotation();
        if (rotationAngle == 90 || rotationAngle == 270) {
            return new PDRectangle(mediaBox.getLowerLeftY(), mediaBox.getLowerLeftX(),
                    mediaBox.getHeight(), mediaBox.getWidth());
        } else {
            return mediaBox;
        }
    }

    /**
     * This will find the CropBox with rotation applied, for this page by looking up the hierarchy
     * until it finds them.
     *
     * @return The CropBox at this level in the hierarchy.
     */
    public static PDRectangle getRotatedCropBox(PDPage page)
    {
        PDRectangle cropBox = page.getCropBox();
        int rotationAngle = page.getRotation();
        if (rotationAngle == 90 || rotationAngle == 270) {
            return new PDRectangle(cropBox.getLowerLeftY(), cropBox.getLowerLeftX(),
                    cropBox.getHeight(), cropBox.getWidth());
        } else {
            return cropBox;
        }
    }
}
