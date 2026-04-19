package net.furizon.zebra_proxy.infrastructure.selenium;

import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebdriverConfiguration {
    @NotNull
    private final WebdriverConfig config;

    public static WebDriver WEB_DRIVER;

    @Bean
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver", config.getChromeDriverPath());
        ChromeOptions options = new ChromeOptions();
        options.setBinary(config.getChromeBinaryPath());
        options.addArguments("--headless");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-web-security");

        var driver = new ChromeDriver(options);
        WEB_DRIVER = driver;
        return driver;
    }

    @PreDestroy
    public void onDestroy() {
        log.info("Closing webdriver");
        WEB_DRIVER.quit();
    }
}
