package net.furizon.zebra_proxy.infrastructure.selenium;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebdriverUtils {
    public static final void waitForPageLoad(@NotNull WebDriver driver, long timeoutMs) throws TimeoutException {
        final String jsCheck =
                "return document.readyState === 'complete' && " + // 1. DOM and standard resources
                "document.fonts.status === 'loaded' && " +        // 2. All fonts are fully loaded
                "Array.from(document.images).every(img => img.complete);"; // 3. All image elements are complete

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeoutMs));
        wait.until(webDriver ->
                (Boolean) ((JavascriptExecutor) webDriver).executeScript(jsCheck)
        );
    }
}
