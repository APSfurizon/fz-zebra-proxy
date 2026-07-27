package net.furizon.zebra_proxy.features.printing.dto;

import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.enumerations.SharpeningLevel;
import com.zebra.sdk.common.card.graphics.containers.internal.ImageAdjustmentLevels;
import com.zebra.sdk.common.card.graphics.enumerations.MonochromeConversion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import net.furizon.zebra_proxy.infrastructure.zebraUtils.ZebraUtils;
import org.jetbrains.annotations.Nullable;

@Data
public class ZebraPrinterConfig {
    @NotNull
    private final String name;
    @NotNull
    private final String ip;

    @NotNull
    private final PrintType printType;
    @NotNull
    private final MonochromeConversion monochromeConversion;
    @NotNull
    private final SharpeningLevel sharpeningLevel;

    @Nullable
    private final String colorProfile;

    @Nullable private final Integer brightness;
    @Nullable private final Integer contrast;
    @Nullable private final Integer gamma;
    @Nullable private final Integer saturation;

    @Nullable private final Integer red;
    @Nullable private final Integer green;
    @Nullable private final Integer blue;

    @NotNull
    private final Integer colorPreheat;
    @NotNull
    private final Integer kPreheat;

    public @NotNull ImageAdjustmentLevels getImgageAdjustmentLevels() {
        ImageAdjustmentLevels imgAdjLevels = new ImageAdjustmentLevels();
        if (brightness != null) {
            ZebraUtils.setBrightnessLevel(imgAdjLevels, brightness);
        }
        if (contrast != null) {
            ZebraUtils.setContrastLevel(imgAdjLevels, contrast);
        }
        if (gamma != null) {
            ZebraUtils.setGammaLevel(imgAdjLevels, gamma);
        }
        if (saturation != null) {
            ZebraUtils.setSaturationLevel(imgAdjLevels, saturation);
        }
        if (red != null && green != null && blue != null) {
            ZebraUtils.setColorScale(imgAdjLevels, red, green, blue);
        }
        return imgAdjLevels;
    }
}
