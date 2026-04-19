package net.furizon.zebra_proxy.infrastructure.security.token.encoder;

import net.furizon.zebra_proxy.infrastructure.security.token.TokenMetadata;
import org.jetbrains.annotations.NotNull;

public interface TokenEncoder {
    @NotNull String encode(@NotNull TokenMetadata metadata);
}
