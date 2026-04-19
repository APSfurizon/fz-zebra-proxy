package net.furizon.zebra_proxy.infrastructure.http.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;

public interface HttpClient {
    /**
     * @throws org.springframework.web.client.HttpClientErrorException if the response status code is not 2xx
     * @throws org.springframework.web.client.RestClientException      if there is an error while executing the request
     */
    @NotNull
    <C extends HttpConfig, R> ResponseEntity<R> send(
            @NotNull final Class<C> configClass,
            @NotNull final HttpRequest<R> request
    );

    /**
     * @throws org.springframework.web.client.HttpClientErrorException if the response status code is not 2xx
     * @throws org.springframework.web.client.RestClientException      if there is an error while executing the request
     */
    @NotNull
    <C extends HttpConfig, R, E> HttpResponse<R, E> send(
        @NotNull final Class<C> configClass,
        @NotNull final HttpRequest<R> request,
        @NotNull final Class<E> errorClass
    );
}
