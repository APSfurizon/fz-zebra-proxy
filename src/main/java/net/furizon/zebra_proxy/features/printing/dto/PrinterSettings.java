package net.furizon.zebra_proxy.features.printing.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class PrinterSettings {
    @NotNull
    private String printerName;
}
