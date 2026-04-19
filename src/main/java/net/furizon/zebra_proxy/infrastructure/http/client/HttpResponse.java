package net.furizon.zebra_proxy.infrastructure.http.client;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Objects;

@AllArgsConstructor
public class HttpResponse<R, E> {
    @Nullable private final ResponseEntity<R> respObj;

    @Nullable private final ClientHttpResponse errorResponse;
    @Nullable private final E errObj;

    @NotNull
    public final ResponseEntity<R> getResponseEntity() {
        return Objects.requireNonNull(respObj);
    }
    @NotNull
    public final ClientHttpResponse getErrorResponse() {
        return Objects.requireNonNull(errorResponse);
    }
    @NotNull
    public final E getErrorEntity() {
        return Objects.requireNonNull(errObj);
    }

    public boolean isError() {
        return errObj != null;
    }
}
