package net.furizon.zebra_proxy.infrastructure.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class HttpErrorResponse {
    @NotNull
    private final List<ApiError> errors;

    @NotNull
    private final String requestId;
}
