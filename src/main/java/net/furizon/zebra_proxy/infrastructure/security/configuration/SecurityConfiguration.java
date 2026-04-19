package net.furizon.zebra_proxy.infrastructure.security.configuration;

import net.furizon.zebra_proxy.infrastructure.security.filter.InternalBasicFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final InternalBasicFilter internalBasicFilter;

    private final SecurityConfig securityConfig;

    @Bean
    public SecurityFilterChain internalFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/job/**")
                .cors(AbstractHttpConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .addFilterAt(
                        internalBasicFilter,
                        BasicAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Map the allowed endpoints
        return http
                .cors(customizer ->
                        customizer.configurationSource(corsConfigurationSource())
                )
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/docs/**"),
                                antMatcher(HttpMethod.GET, "/swagger-ui/**")
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        final var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(securityConfig.getAllowedOrigins());
        corsConfiguration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.OPTIONS.name()
                )
        );
        corsConfiguration.setAllowedHeaders(
                List.of("*")
        );
        corsConfiguration.setAllowCredentials(true);

        final var urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return urlBasedCorsConfigurationSource;
    }
}
