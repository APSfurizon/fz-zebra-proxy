package net.furizon.zebra_proxy.features.printing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.zebra_proxy.features.printing.dto.PrintIdContentPair;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintingService {
    @NotNull
    private final WebDriver webDriver;

    public void invoke(@NotNull PrintIdContentPair pair) {
        log.info("Printing {}", pair);
    }
}
