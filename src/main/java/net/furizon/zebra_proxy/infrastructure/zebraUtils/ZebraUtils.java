package net.furizon.zebra_proxy.infrastructure.zebraUtils;

import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.graphics.ZebraCardImage;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.containers.internal.ImageAdjustmentLevels;
import com.zebra.sdk.common.card.graphics.enumerations.MonochromeConversion;
import com.zebra.sdk.common.card.graphics.enumerations.PrinterModel;
import com.zebra.sdk.common.card.graphics.enumerations.internal.ImageAdjustType;
import com.zebra.sdk.common.card.graphics.utilities.internal.ImageUtils;
import com.zebra.sdk.common.card.graphics.utilities.internal.Utilities;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.settings.SettingsException;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;

public class ZebraUtils {
    public static @Nullable PrinterModel getPrinterModel(@NotNull ZebraCardPrinter printer) throws ConnectionException, SettingsException, ZebraCardException {
        String model = printer.getPrinterInformation().model;
        if (model.equalsIgnoreCase("ZXP11")) {
            return PrinterModel.ZXPSeries1;
        } else if (!model.toLowerCase(Locale.US).contains("zxp31") && !model.toLowerCase(Locale.US).contains("zxp32")) {
            if (model.equalsIgnoreCase("ZXP Series 7")) {
                return PrinterModel.ZXPSeries7;
            } else if (model.equalsIgnoreCase("ZXP Series 8")) {
                return PrinterModel.ZXPSeries8;
            } else if (model.equalsIgnoreCase("ZXP Series 9")) {
                return PrinterModel.ZXPSeries9;
            } else if (!model.equalsIgnoreCase("ZC100") && !model.equalsIgnoreCase("ZC150")) {
                if (model.equalsIgnoreCase("ZC300") || model.equalsIgnoreCase("ZC350")) {
                    return PrinterModel.ZC300;
                }
            } else {
                return PrinterModel.ZC100;
            }
        } else {
            return PrinterModel.ZXPSeries3;
        }
        return null;
    }

    public static int getMaxHeight(int h, @NotNull PrinterModel printerModel, @NotNull OrientationType orientation) {
        int maxH = 0;
        switch (printerModel) {
            case ZXPSeries7:
            case ZC100:
            case ZC300:
                if (orientation == OrientationType.Landscape) {
                    maxH = 640;
                } else {
                    maxH = 1006;
                }
                break;
            case ZXPSeries1:
            case ZXPSeries3:
                if (orientation == OrientationType.Landscape) {
                    maxH = 640;
                } else {
                    maxH = 1024;
                }
                break;
            case ZXPSeries8:
            case ZXPSeries9:
                if (orientation == OrientationType.Landscape) {
                    maxH = 648;
                } else {
                    maxH = 1024;
                }
                break;
            case ZXPSeries9_600:
                if (orientation == OrientationType.Landscape) {
                    maxH = 1296;
                } else {
                    maxH = 2046;
                }
        }

        if (h > 0 && h <= maxH) {
            maxH = h;
        }

        return maxH;
    }

    public static int getMaxWidth(int w, @NotNull PrinterModel printerModel, @NotNull OrientationType orientation) {
        int maxW = 0;
        switch (printerModel) {
            case ZXPSeries7:
            case ZC100:
            case ZC300:
                if (orientation == OrientationType.Landscape) {
                    maxW = 1006;
                } else {
                    maxW = 640;
                }
                break;
            case ZXPSeries1:
            case ZXPSeries3:
                if (orientation == OrientationType.Landscape) {
                    maxW = 1024;
                } else {
                    maxW = 640;
                }
                break;
            case ZXPSeries8:
            case ZXPSeries9:
                if (orientation == OrientationType.Landscape) {
                    maxW = 1024;
                } else {
                    maxW = 648;
                }
                break;
            case ZXPSeries9_600:
                if (orientation == OrientationType.Landscape) {
                    maxW = 2046;
                } else {
                    maxW = 1296;
                }
        }

        if (w > 0 && w <= maxW) {
            maxW = w;
        }

        return maxW;
    }

    public static @NotNull ZebraCardImageI convertToimage(@NotNull BufferedImage sourceImg,
                                                          @Nullable ImageAdjustmentLevels imgAdjLevels,
                                                          @NotNull PrinterModel printerModel, @NotNull OrientationType orientation,// @NotNull RotationType rotation,
                                                          @NotNull PrintType printType, @NotNull MonochromeConversion monoConversionType,
                                                          @Nullable RenderingHints renderingHints,
                                                          @Nullable String colorProfile) throws IOException {
        ZebraCardImageI outImage = null;
        Graphics2D graphics2D = null;
        ByteArrayOutputStream outData = new ByteArrayOutputStream();

        try {
            int maxW = getMaxWidth(0, printerModel, orientation);
            int maxH = getMaxHeight(0, printerModel, orientation);
            BufferedImage image = new BufferedImage(maxW, maxH, TYPE_3BYTE_BGR);
            graphics2D = image.createGraphics();
            if (renderingHints != null) {
                graphics2D.setRenderingHints(renderingHints);
            }


            //The min should crop the image
            graphics2D.drawImage(sourceImg, 0, 0, maxW, maxH, 0, 0, Math.min(maxW, sourceImg.getWidth()), Math.min(maxH, sourceImg.getHeight()), (ImageObserver)null);
            image = ImageUtils.applyMonoConversion(image, printType, monoConversionType, renderingHints);
            if (image.getType() == TYPE_3BYTE_BGR) {
                if (imgAdjLevels == null) {
                    imgAdjLevels = new ImageAdjustmentLevels();
                }
                image = ImageUtils.applyImageAdjustments(image, imgAdjLevels);
            }

            if (image.getType() == TYPE_3BYTE_BGR && colorProfile != null && !colorProfile.isEmpty()) {
                image = ImageUtils.applyColorProfile(image, colorProfile);
            }

            ImageIO.write(image, "bmp", outData);
            outImage = new ZebraCardImage(outData.toByteArray());
        } finally {
            IOUtils.closeQuietly(outData);
            if (graphics2D != null) {
                graphics2D.dispose();
            }

        }

        return outImage;
    }


    public static void setBrightnessLevel(@NotNull ImageAdjustmentLevels imgAdjLevels, int brightness) {
        if (brightness >= -25 && brightness <= 25) {
            imgAdjLevels.brightnessLevel = (double) Utilities.convertValue(ImageAdjustType.Brightness, (float)brightness);
        } else {
            throw new IllegalArgumentException(String.format("Brightness value out of range: %d to %d.", -25, 25));
        }
    }

    public static void setContrastLevel(@NotNull ImageAdjustmentLevels imgAdjLevels, int contrast) {
        if (contrast >= -25 && contrast <= 25) {
            imgAdjLevels.contrastLevel = (double)Utilities.convertValue(ImageAdjustType.Contrast, (float)contrast);
        } else {
            throw new IllegalArgumentException(String.format("Contrast value out of range: %d to %d.", -25, 25));
        }
    }

    public static void setGammaLevel(@NotNull ImageAdjustmentLevels imgAdjLevels, int gamma) {
        if (gamma >= -25 && gamma <= 25) {
            imgAdjLevels.gammaLevel = (double)gamma;
        } else {
            throw new IllegalArgumentException(String.format("Gamma value out of range: %d to %d.", -25, 25));
        }
    }

    public static void setSaturationLevel(@NotNull ImageAdjustmentLevels imgAdjLevels, int saturation) {
        if (saturation >= -25 && saturation <= 25) {
            imgAdjLevels.saturationLevel = (double)saturation;
        } else {
            throw new IllegalArgumentException(String.format("Saturation value out of range: %d to %d.", -25, 25));
        }
    }

    public static void setColorScale(@NotNull ImageAdjustmentLevels imgAdjLevels, int red, int green, int blue) {
        if (red >= -25 && red <= 25 && green >= -25 && green <= 25 && blue >= -25 && blue <= 25) {
            imgAdjLevels.redLevel = (double)Utilities.convertValue(ImageAdjustType.ColorScale, (float)red);
            imgAdjLevels.greenLevel = (double)Utilities.convertValue(ImageAdjustType.ColorScale, (float)green);
            imgAdjLevels.blueLevel = (double)Utilities.convertValue(ImageAdjustType.ColorScale, (float)blue);
        } else {
            throw new IllegalArgumentException(String.format("Color scale value out of range: %d to %d.", -25, 25));
        }
    }
}
