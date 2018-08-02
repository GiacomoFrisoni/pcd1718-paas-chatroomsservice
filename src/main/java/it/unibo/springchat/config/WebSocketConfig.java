package it.unibo.springchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/websocket-chat")
				.setAllowedOrigins("*")
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Enables a full featured broker like RabbitMQ
		registry.setApplicationDestinationPrefixes("/app")
				.enableStompBrokerRelay("/channel")
				.setRelayHost("localhost")
				.setRelayPort(5672)
				.setClientLogin("guest")
				.setClientPasscode("guest");
	}

}