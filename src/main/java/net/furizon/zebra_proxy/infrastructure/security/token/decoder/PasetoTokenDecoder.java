package net.furizon.zebra_proxy.infrastructure.security.token.decoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.furizon.zebra_proxy.infrastructure.security.configuration.SecurityConfig;
import net.furizon.zebra_proxy.infrastructure.security.token.TokenMetadata;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.paseto4j.commons.PasetoException;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PasetoTokenDecoder implements TokenDecoder {
    private final ObjectMapper objectMapper;

    private final SecretKey secretKey;

    public PasetoTokenDecoder(SecurityConfig securityConfig, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.secretKey = new SecretKey(
            securityConfig.getTokenSecretKey().getBytes(StandardCharsets.UTF_8),
            Version.V4
        );
    }

    @Override
    public @NotNull TokenMetadata decode(@NotNull String token) {
        try {
            final var json = Paseto.decrypt(
                secretKey,
                token,
                "fz zebra proxy"
            );

            return objectMapper.readValue(json, TokenMetadata.class);
        } catch (JsonProcessingException ex) {
            log.warn("Could read json: {}", ex.getMessage());
            throw new AuthenticationServiceException("Bad message", ex);
        } catch (PasetoException e) {
            log.warn("Could not decode token: {}", e.getMessage());
            throw new BadCredentialsException("Invalid Session Token");
        }
    }
}
