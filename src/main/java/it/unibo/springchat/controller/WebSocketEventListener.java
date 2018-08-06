package it.unibo.springchat.controller;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.model.OrderedChatMessage;

@Component
public class WebSocketEventListener {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

	@Autowired
    private TicketDispenserClient ticketDispenserClient;
	
	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	@EventListener
	public void handleWebSocketConnectListener(final SessionConnectedEvent event) {
		logger.info("Received a new web socket connection.");
	}

	@EventListener
	public void handleWebSocketDisconnectListener(final SessionDisconnectEvent event) {
		// Retrieves and checks session data for the socket connection
		final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		final String username = (String) headerAccessor.getSessionAttributes().get("username");
		final String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");
		if (username != null && roomId != null) {
			// Sends a ordered logout message to the clients connected in the same room
			final OrderedChatMessage leaveMessage = new OrderedChatMessage(
					MessageType.LEAVE,
					username,
					this.ticketDispenserClient.getTicket(roomId));
			messagingTemplate.convertAndSend(format("/topic/channel.%s", roomId), leaveMessage);
			logger.info("User " + username + " disconnected from room " + roomId);
		}
	}
	
}
