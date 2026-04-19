package net.furizon.zebra_proxy.infrastructure.security.configuration;

import net.furizon.zebra_proxy.infrastructure.concurrent.MdcTaskDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static net.furizon.zebra_proxy.infrastructure.security.Const.SESSION_THREAD_POOL_TASK_EXECUTOR;

@Configuration
@RequiredArgsConstructor
public class SecurityThreadConfiguration {
    private final SecurityConfig securityConfig;

    @Bean(SESSION_THREAD_POOL_TASK_EXECUTOR)
    public Executor sessionThreadPoolTaskExecutor() {
        int corePoolUpdateSize = securityConfig.getSession().getCorePoolUpdateSize();
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolUpdateSize);
        executor.setMaxPoolSize(corePoolUpdateSize);
        executor.setThreadNamePrefix("session-thread-");
        executor.setTaskDecorator(new MdcTaskDecorator());

        return executor;
    }
}
