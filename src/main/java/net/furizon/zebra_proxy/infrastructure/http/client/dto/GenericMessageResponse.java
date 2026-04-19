package net.furizon.zebra_proxy.infrastructure.http.client.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GenericMessageResponse {
    @NotNull
    private final String message;
}
