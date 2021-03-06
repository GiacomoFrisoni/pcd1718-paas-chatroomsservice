package it.unibo.springchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Bean
    public HttpSessionIdHandshakeInterceptor handshakeInterceptor() {
        return new HttpSessionIdHandshakeInterceptor();
    }
	
	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		// Registers a STOMP end-point over websockets with Sock.js enabled
		registry.addEndpoint("/websocket-chat")
				.addInterceptors(handshakeInterceptor())
				.setAllowedOrigins("*")
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry registry) {
		/*
		 * Defines a prefix for destinations handled by the application itself.
		 */
		registry.setApplicationDestinationPrefixes("/app");
		/*
		 * Enables a full featured broker based on RabbitMQ.
		 * For RabbitMQ STOMP support (and 61613 port enabling), it is required the execution of this command:
		 * rabbitmq-plugins enable rabbitmq_stomp.
		 * NOTE: RabbitMQ does not support "/" as a destination separator.
		 */
		registry.enableStompBrokerRelay("/queue", "/topic")
				.setRelayHost("localhost")
				.setRelayPort(61613)
				.setClientLogin("guest")
				.setClientPasscode("guest");
	}

}