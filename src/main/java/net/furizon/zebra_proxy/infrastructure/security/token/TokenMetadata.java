package net.furizon.zebra_proxy.infrastructure.security.token;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
public class TokenMetadata {
    private final long userId;

    private final UUID sessionId;
}
