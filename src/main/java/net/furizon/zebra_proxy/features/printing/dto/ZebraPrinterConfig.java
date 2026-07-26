package net.furizon.zebra_proxy.features.printing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ZebraPrinterConfig {
    @NotNull
    private final String name;
    @NotNull
    private final String ip;
}
