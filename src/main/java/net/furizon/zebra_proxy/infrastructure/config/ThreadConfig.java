package net.furizon.zebra_proxy.infrastructure.config;

import net.furizon.zebra_proxy.infrastructure.concurrent.MdcTaskDecorator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

@EnableAsync
@Configuration
@ConditionalOnProperty(
    value = "spring.thread-executor",
    havingValue = "virtual"
)
public class ThreadConfig {
    @Bean
    @Primary
    public AsyncTaskExecutor applicationTaskExecutor() {
        final var executor = new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
        executor.setTaskDecorator(new MdcTaskDecorator());

        return executor;
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
