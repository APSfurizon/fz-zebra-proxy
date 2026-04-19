package net.furizon.zebra_proxy.infrastructure.web.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ApiError {
    @NotNull
    private final String message;

    @NotNull
    private final String code;

    public ApiError(@NotNull String message, @NotNull Enum<?> code) {
        this.message = message;
        this.code = code.name();
    }

    public ApiError(@NotNull String message, @NotNull String code) {
        this.message = message;
        this.code = code;
    }
}
