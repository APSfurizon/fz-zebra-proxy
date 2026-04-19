package net.furizon.zebra_proxy.infrastructure.security.token.encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.furizon.zebra_proxy.infrastructure.security.configuration.SecurityConfig;
import net.furizon.zebra_proxy.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PasetoTokenEncoder implements TokenEncoder {

    private final ObjectMapper objectMapper;

    private final SecretKey secretKey;

    public PasetoTokenEncoder(SecurityConfig securityConfig, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.secretKey = new SecretKey(
            securityConfig.getTokenSecretKey().getBytes(StandardCharsets.UTF_8),
            Version.V4
        );
    }


    @Override
    public @NotNull String encode(@NotNull TokenMetadata metadata) {
        String json;
        try {
            json = objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Paseto.encrypt(
            secretKey,
            json,
            "fz zebra proxy"
        );
    }
}
