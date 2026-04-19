package net.furizon.zebra_proxy.infrastructure.http.client.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GenericErrorResponse {
    @NotNull
    private final String error;
}
