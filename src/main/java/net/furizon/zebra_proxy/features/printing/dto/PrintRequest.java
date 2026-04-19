package net.furizon.zebra_proxy.features.printing.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
public class PrintRequest {
    @NotEmpty
    @ToString.Exclude
    private final String html;

    @NotEmpty
    private final String id;

    @NotNull
    private final Long operatorID;

    @NotNull
    private final PrintType type;

    public @NotNull QueuePair toQueuePair() {
        return QueuePair.of(operatorID, type);
    }

    public @NotNull PrintIdContentPair toIdContentPair() {
        return PrintIdContentPair.of(id, html);
    }
}
