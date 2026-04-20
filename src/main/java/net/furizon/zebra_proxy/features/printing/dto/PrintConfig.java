package net.furizon.zebra_proxy.features.printing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class PrintConfig {
    // Operator -> (print type -> printer settings)
    @NotNull
    private final Map<Long, Map<PrintType, PrinterSettings>> printers;
}
