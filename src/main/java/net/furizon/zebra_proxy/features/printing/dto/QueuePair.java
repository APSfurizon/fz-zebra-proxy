package net.furizon.zebra_proxy.features.printing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueuePair {
    private final long operatorID;
    @NotNull private final PrintType printType;

    public static @NotNull QueuePair of(final long operatorID, final PrintType printType) {
        return new QueuePair(operatorID, printType);
    }
}
