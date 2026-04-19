package net.furizon.zebra_proxy.infrastructure.config;

import jakarta.servlet.Filter;
import net.furizon.zebra_proxy.infrastructure.web.filter.CorrelationIdFilter;
import net.furizon.zebra_proxy.infrastructure.web.filter.MdcFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.PriorityOrdered;

@Configuration
public class FilterChainConfiguration {
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        return createFilterRegistrationBean(new CorrelationIdFilter());
    }

    @Bean
    public FilterRegistrationBean<MdcFilter> mdcFilter() {
        return createFilterRegistrationBean(new MdcFilter());
    }

    private <T extends Filter & PriorityOrdered> FilterRegistrationBean<T> createFilterRegistrationBean(T filter) {
        final var registration = new FilterRegistrationBean<T>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName(filter.getClass().getCanonicalName());
        registration.setOrder(filter.getOrder());

        return registration;
    }
}
