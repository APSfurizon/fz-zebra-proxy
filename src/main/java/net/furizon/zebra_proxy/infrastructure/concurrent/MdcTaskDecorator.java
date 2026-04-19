package net.furizon.zebra_proxy.infrastructure.concurrent;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public @NotNull Runnable decorate(@NotNull Runnable runnable) {
        // Capture the MDC context of the current thread
        final var contextMap = MDC.getCopyOfContextMap();

        return () -> {
            // Set the MDC context for the new thread
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            } else {
                MDC.clear();
            }

            try {
                // Run the actual task
                runnable.run();
            } finally {
                // Clear the MDC context after the task completes
                MDC.clear();
            }
        };
    }
}
