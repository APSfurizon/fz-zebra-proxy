package net.furizon.zebra_proxy.infrastructure.security.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.core.BodyFilters;

import static java.util.Collections.singleton;
import static org.zalando.logbook.BodyFilter.merge;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

@Configuration
public class LogbookFilterConfiguration {
    // NOTICE: All passwords needs to be named "password" to be
    // mathced by LogbookFilterConfiguration
    // and be hidden in logs
    @Bean
    public BodyFilter bodyFilter() {
        return merge(
            BodyFilters.defaultValue(),
            replaceJsonStringProperty(singleton("password"), "XXX"));
    }
}
