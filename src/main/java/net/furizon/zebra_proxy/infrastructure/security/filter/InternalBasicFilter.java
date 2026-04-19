package net.furizon.zebra_proxy.infrastructure.security.filter;

import net.furizon.zebra_proxy.infrastructure.security.configuration.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalBasicFilter extends OncePerRequestFilter {
    private final SecurityConfig securityConfig;

    private static final String INTERNAL_API_PERMISSION = "api:internal";

    private static final List<GrantedAuthority> INTERNAL_API_PERMISSION_AUTHORITIES = Collections.singletonList(
        (GrantedAuthority) () -> INTERNAL_API_PERMISSION
    );

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.matches("(?i)^Basic .*")) {
            filterChain.doFilter(request, response);
            //log.warn("Bad basic auth header: {} on request {}", authHeader, request.getRequestURI());
            return;
        }

        try {
            final var decoded = Base64.getDecoder().decode(
                authHeader.replaceFirst("(?i)^Basic ", "")
            );
            final var basicData = new String(decoded, StandardCharsets.UTF_8).split(":");
            final var username = basicData[0];
            final var password = basicData[1];

            if (!username.equals(securityConfig.getInternal().getUsername())
                || !password.equals(securityConfig.getInternal().getPassword())) {
                log.warn("Username or password do not match in basic auth, on request {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            SecurityContextHolder
                .getContext()
                .setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        null,
                        username,
                        INTERNAL_API_PERMISSION_AUTHORITIES
                    )
                );
        } catch (Exception e) {
            log.warn("An error happened while processing Basic Authentication", e);
        } finally {
            filterChain.doFilter(request, response);
            SecurityContextHolder.clearContext();
        }
    }
}
