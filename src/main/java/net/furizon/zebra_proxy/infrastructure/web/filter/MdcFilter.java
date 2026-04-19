package net.furizon.zebra_proxy.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static net.furizon.zebra_proxy.infrastructure.web.Web.Constants.FilterOrders.MDC_PRECEDENCE;
import static net.furizon.zebra_proxy.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@Order(MDC_PRECEDENCE)
public class MdcFilter
    extends OncePerRequestFilter
    implements PriorityOrdered {
    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String correlationId = (String) request.getAttribute(MDC_CORRELATION_ID);
        if (correlationId != null) {
            MDC.put(MDC_CORRELATION_ID, correlationId);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return MDC_PRECEDENCE;
    }
}
