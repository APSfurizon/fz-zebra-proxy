package net.furizon.zebra_proxy.infrastructure.frontend;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FrontendMessage {

    @Value("${server.port}")
    private Integer port;

    @PostConstruct
    public void init() {
        log.info("======================================================");
        log.info("Hosting frontend at: http://{}:{}/frontend", InetAddress.getLoopbackAddress().getHostAddress(), port);
        log.info("======================================================");
    }
}
