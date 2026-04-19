package net.furizon.zebra_proxy.infrastructure.selenium;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "webdriver")
public class WebdriverConfig {
    @NotNull
    private final String chromeDriverPath;

    @NotNull
    private final String chromeBinaryPath;
}
