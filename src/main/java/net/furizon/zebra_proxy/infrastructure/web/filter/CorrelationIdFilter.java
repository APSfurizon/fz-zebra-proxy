package net.furizon.zebra_proxy.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static net.furizon.zebra_proxy.infrastructure.web.Web.Constants.FilterOrders.CORRELATION_ID_PRECEDENCE;
import static net.furizon.zebra_proxy.infrastructure.web.Web.Constants.Headers.X_CORRELATION_ID_HEADER;
import static net.furizon.zebra_proxy.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@Slf4j
@Order(CORRELATION_ID_PRECEDENCE)
public class CorrelationIdFilter
    extends OncePerRequestFilter
    implements PriorityOrdered {
    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var attributedCorrelationId = (String) request.getAttribute(MDC_CORRELATION_ID);
        final var correlationId = attributedCorrelationId != null
            ? attributedCorrelationId
            : getOrGenerateCorrelationId(request);

        request.setAttribute(MDC_CORRELATION_ID, correlationId);
        response.setHeader(X_CORRELATION_ID_HEADER, correlationId);
        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return CORRELATION_ID_PRECEDENCE;
    }

    @NotNull
    private String getOrGenerateCorrelationId(@NotNull HttpServletRequest request) {
        final var headerCorrelationId = request.getHeader(X_CORRELATION_ID_HEADER);
        if (headerCorrelationId == null) {
            final var uuid = UUID.randomUUID().toString();
            log.trace("Generating request a new correlationId: {}", uuid);
            return uuid;
        }

        return headerCorrelationId;
    }
}
