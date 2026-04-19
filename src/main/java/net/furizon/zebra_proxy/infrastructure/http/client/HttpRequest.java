package net.furizon.zebra_proxy.infrastructure.http.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class HttpRequest<R> {
    @NotNull
    private final HttpMethod method;

    @NotNull
    private final String path;

    @NotNull
    private final MultiValueMap<String, String> headers;

    @NotNull
    private final Map<String, ?> uriVariables;

    @NotNull
    private final MultiValueMap<String, String> queryParams;

    @Nullable
    private final Object body;

    @Nullable
    private final MediaType contentType;

    @Nullable
    private final Class<R> responseType;

    @Nullable
    private final ParameterizedTypeReference<R> responseParameterizedType;

    @Nullable
    private final Pair<String, String> basicAuth;
    private final boolean basicAuthSet;

    @Nullable
    @Getter(AccessLevel.NONE)
    private final String overrideBaseUrl;
    @NotNull public String overrideBaseUrl() {
        return overrideBaseUrl != null ? overrideBaseUrl : "";
    }
    public boolean shouldOverrideUrl() {
        return overrideBaseUrl != null;
    }

    @Nullable
    @Getter(AccessLevel.NONE)
    private final String overrideBasePath;
    @NotNull public String overrideBasePath() {
        return overrideBasePath != null ? overrideBasePath : "";
    }
    public boolean shouldOverridePath() {
        return overrideBasePath != null;
    }

    //Overriding the getter for naming reasons
    @Getter(AccessLevel.NONE)
    private final boolean sendConfigHeaders;
    public final boolean sendConfigHeaders() {
        return sendConfigHeaders;
    }

    public static class Builder<R> {
        private Class<R> responseType;
        private String path = null;
        private Object body = null;
        private HttpMethod method = null;
        private MediaType contentType = null;
        private String overrideBaseUrl = null;
        private String overrideBasePath = null;
        private boolean sendConfigHeaders = true;
        private Pair<String, String> basicAuth = null;
        private boolean basicAuthSet = false;
        private ParameterizedTypeReference<R> responseParameterizedType;

        private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        private final Map<String, String> uriVariables = new LinkedHashMap<>();
        private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        public Builder<R> sendConfigHeaders(boolean sendConfigHeaders) {
            this.sendConfigHeaders = sendConfigHeaders;
            return this;
        }

        public Builder<R> overrideBasePath(@NotNull String overrideBasePath) {
            this.overrideBasePath = overrideBasePath;
            return this;
        }

        public Builder<R> overrideBaseUrl(@NotNull String overrideBaseUrl) {
            this.overrideBaseUrl = overrideBaseUrl;
            return this;
        }

        public Builder<R> method(@NotNull final HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder<R> path(@NotNull final String path) {
            this.path = path;
            return this;
        }

        public Builder<R> header(@NotNull final String key, @NotNull final String value) {
            this.headers.add(key, value);
            return this;
        }

        public Builder<R> headers(@NotNull final MultiValueMap<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder<R> uriVariable(@NotNull final String key, @NotNull final String value) {
            this.uriVariables.put(key, value);
            return this;
        }

        public Builder<R> queryParam(@NotNull final String key, @NotNull final String value) {
            this.queryParams.add(key, value);
            return this;
        }

        public Builder<R> queryParams(@NotNull final MultiValueMap<String, String> queryParams) {
            this.queryParams.addAll(queryParams);
            return this;
        }

        public Builder<R> contentType(@NotNull final MediaType mediaType) {
            this.contentType = mediaType;
            return this;
        }

        public Builder<R> body(@NotNull final Object body) {
            this.body = body;
            return this;
        }

        public Builder<R> responseType(@NotNull final Class<R> responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder<R> responseParameterizedType(
            @NotNull final ParameterizedTypeReference<R> responseParameterizedType
        ) {
            this.responseParameterizedType = responseParameterizedType;
            return this;
        }

        //We allow null auth config on purpose
        public Builder<R> basicAuth(@Nullable final Pair<String, String> basicAuth) {
            this.basicAuth = basicAuth;
            this.basicAuthSet = true;
            return this;
        }
        public Builder<R> basicAuth(@NotNull final String username, @NotNull final String password) {
            this.basicAuth = new ImmutablePair<>(username, password);
            basicAuthSet =  true;
            return this;
        }

        public HttpRequest<R> build() {
            return new HttpRequest<>(
                method,
                path,
                headers,
                uriVariables,
                queryParams,
                body,
                contentType,
                responseType,
                responseParameterizedType,
                basicAuth,
                basicAuthSet,
                overrideBaseUrl,
                overrideBasePath,
                sendConfigHeaders
            );
        }
    }

    public static <R> Builder<R> create() {
        return new Builder<>();
    }
}
