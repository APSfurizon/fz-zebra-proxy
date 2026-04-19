package net.furizon.zebra_proxy.infrastructure.http.client;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public interface HttpConfig {
    @NotNull
    String getBaseUrl();

    @NotNull
    default MultiValueMap<String, String> headers() {
        return new LinkedMultiValueMap<>();
    }

    @NotNull
    default String getBasePath() {
        return "";
    }

    @Nullable
    default Pair<String, String> basicAuth() {
        return null;
    }
}
