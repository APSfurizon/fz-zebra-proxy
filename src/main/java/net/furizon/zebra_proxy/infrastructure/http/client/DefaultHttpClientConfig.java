package net.furizon.zebra_proxy.infrastructure.http.client;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static net.furizon.zebra_proxy.infrastructure.http.client.HttpClientConstants.DEFAULT_HTTP_CLIENT;


@Data
@ConfigurationProperties(prefix = DEFAULT_HTTP_CLIENT)
public class DefaultHttpClientConfig implements HttpConfig {
    @Override
    public @NotNull String getBaseUrl() {
        return "";
    }
}
