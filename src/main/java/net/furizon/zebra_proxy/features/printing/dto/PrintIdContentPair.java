package net.furizon.zebra_proxy.features.printing.dto;

import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
public class PrintIdContentPair {
    @NotNull
    private final String printId;
    @NotNull
    @ToString.Exclude
    private final String html;

    public static @NotNull PrintIdContentPair of(final String printId, final String html) {
        return new PrintIdContentPair(printId, html);
    }
}
