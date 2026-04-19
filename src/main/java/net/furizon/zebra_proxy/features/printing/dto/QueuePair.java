package net.furizon.zebra_proxy.features.printing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueuePair {
    public final long operatorID;
    @NotNull public final PrintType printType;

    public static @NotNull QueuePair of(final long operatorID, final PrintType printType) {
        return new QueuePair(operatorID, printType);
    }
}
