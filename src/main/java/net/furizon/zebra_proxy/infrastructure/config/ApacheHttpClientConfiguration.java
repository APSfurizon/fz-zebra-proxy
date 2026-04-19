package net.furizon.zebra_proxy.infrastructure.config;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient5.LogbookHttpExecHandler;

import static net.furizon.zebra_proxy.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@Configuration
public class ApacheHttpClientConfiguration {
    @Bean
    @Scope(scopeName = "prototype")
    public HttpClientBuilder httpClientBuilder(@NotNull final Logbook logbook) {
        return HttpClients.custom()
            .addExecInterceptorFirst("Logbook", new LogbookHttpExecHandler(logbook))
            .disableAutomaticRetries();
    }

    @Bean
    @Primary
    public CorrelationId correlationId() {
        return request -> MDC.get(MDC_CORRELATION_ID);
    }
}
