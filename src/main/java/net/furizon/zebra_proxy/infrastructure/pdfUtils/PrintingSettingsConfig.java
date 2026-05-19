package net.furizon.zebra_proxy.infrastructure.pdfUtils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("print")
public class PrintingSettingsConfig {

    private final Card card;

    @Data
    public static class Card {
        private final double width;
        private final double height;
        private final boolean invertMediaOrientation;
        private final boolean invertPageformatOrientation;
    }
}
