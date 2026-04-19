package net.furizon.zebra_proxy.infrastructure.web;

import org.springframework.core.Ordered;

public class Web {
    public static class Constants {
        public static class Headers {
            public static final String X_CORRELATION_ID_HEADER = "X-Correlation-Id";
        }

        public static class Mdc {
            public static final String MDC_CORRELATION_ID = "correlationId";
        }

        public static class FilterOrders {
            public static final int CORRELATION_ID_PRECEDENCE = Ordered.HIGHEST_PRECEDENCE;
            public static final int MDC_PRECEDENCE = CORRELATION_ID_PRECEDENCE + 1;
        }
    }
}
