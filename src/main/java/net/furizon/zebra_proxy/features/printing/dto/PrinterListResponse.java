package net.furizon.zebra_proxy.features.printing.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class PrinterListResponse {
    @NotNull
    private final List<String> printers;

    @Nullable
    private final String defaultPrinter;
}
