package net.furizon.zebra_proxy.infrastructure.security.token.decoder;

import net.furizon.zebra_proxy.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;

public interface TokenDecoder {
    @NotNull TokenMetadata decode(@NotNull String token);
}
