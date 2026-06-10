package com.jkh1447.MyProject.global.config.webSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import com.jkh1447.MyProject.global.config.ServerConfig;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final ServerConfig serverConfig;
    private final HandshakeAuthInterceptor handshakeAuthInterceptor;
    public WebSocketConfig(@Lazy StompHandler stompHandler, ServerConfig serverConfig, HandshakeAuthInterceptor handshakeAuthInterceptor) {
        this.stompHandler = stompHandler;
        this.serverConfig = serverConfig;
        this.handshakeAuthInterceptor = handshakeAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[] {0, 0});
                //.setTaskScheduler(heartbeatScheduler());
        config.setApplicationDestinationPrefixes("/app"); // 서버의 웹소켓 요청주소 prefix

        config.setUserDestinationPrefix("/user");
    }

    @Bean
    public TaskScheduler heartbeatScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 배포시 cors설정
        registry.addEndpoint("/ws-matching").setAllowedOriginPatterns(serverConfig.getUrl())
                .addInterceptors(handshakeAuthInterceptor);
    }

    // 구독 연결시 실행
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler)
                        .taskExecutor()             // ← 추가
                        .corePoolSize(8)
                        .maxPoolSize(32)
                        .queueCapacity(1000);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                        .corePoolSize(8)
                        .maxPoolSize(32)
                        .queueCapacity(1000);
    }

}
