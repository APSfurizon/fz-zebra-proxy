package net.furizon.zebra_proxy.infrastructure.security.configuration;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Data
@ConfigurationProperties("security")
public class SecurityConfig {
    @NotNull
    private final String passwordSalt;

    @NotNull
    private final String tokenSecretKey;

    @NotNull
    private final List<String> allowedOrigins;

    @NotNull
    private final Session session;

    @NotNull
    private final Internal internal;

    private final short maxFailedLoginAttempts;

    @Data
    public static class Session {
        private final int corePoolUpdateSize;
    }

    @Data
    public static class Internal {
        @NotNull
        private final String username;

        @NotNull
        private final String password;
    }
}
