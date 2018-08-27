package it.unibo.springchat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import it.unibo.springchat.utility.Consts;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Class used in order to obtain the session id by Spring Interceptor.
 * Source: https://www.baeldung.com/spring-websockets-sendtouser
 * 
 */
public class HttpSessionIdHandshakeInterceptor implements HandshakeInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(HttpSessionIdHandshakeInterceptor.class);

	@Override
	public boolean beforeHandshake(final ServerHttpRequest request, final ServerHttpResponse response,
			final WebSocketHandler wsHandler, final Map<String, Object> attributes) throws Exception {
		logger.info("Handshake interceptor called!");
		if (request instanceof ServletServerHttpRequest) {
			final ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
			final HttpSession session = servletRequest.getServletRequest().getSession();
			if (session != null) {
				attributes.put(Consts.HTTP_SESSION_ID_ATTR, session.getId());
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(final ServerHttpRequest request, final ServerHttpResponse response,
							   final WebSocketHandler wsHandler, final Exception ex) {
	}
	
}
