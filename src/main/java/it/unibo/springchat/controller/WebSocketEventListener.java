package it.unibo.springchat.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.utility.Consts;
import it.unibo.springchat.model.OrderedChatMessage;

@Component
public class WebSocketEventListener {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	
	/*
	 * This data structure keeps track of the session identifiers of the clients subscribed
	 * to each room topic.
	 */
	private final Map<String, Set<String>> clientsRooms = new HashMap<>();

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
		final String sessionId = headerAccessor.getSessionId();
		final String username = (String) headerAccessor.getSessionAttributes().get(Consts.SESSION_USERNAME_ATTR);
		final String roomId = (String) headerAccessor.getSessionAttributes().get(Consts.SESSION_ROOM_ID_ATTR);
		if (sessionId != null && username != null && roomId != null) {
			// Unsubscribes the disconnected client, if it was subscribed to a topic room
			unsubscribeClient(sessionId);
			// Sends a ordered logout message to the clients connected in the same room
			final OrderedChatMessage leaveMessage = new OrderedChatMessage(
					MessageType.LEAVE,
					username,
					this.ticketDispenserClient.getTicket(roomId));
			this.messagingTemplate.convertAndSend(Consts.getTopic(roomId), leaveMessage);
			logger.info("User " + username + " disconnected from room " + roomId);
		}
	}
	
	@EventListener
    private void handleSessionSubscribeEvent(final SessionSubscribeEvent event) {
		final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		final String sessionId = headerAccessor.getSessionId();
		final String roomId = Consts.getRoomIdFromTopic(headerAccessor.getDestination());
		if (sessionId != null && roomId != null) {
			// Registers the client topic subscription
			if (!this.clientsRooms.containsKey(roomId)) {
				this.clientsRooms.put(roomId, new HashSet<>());
			}
			this.clientsRooms.get(roomId).add(sessionId);
		}
		logger.info(this.clientsRooms.toString());
    }

    @EventListener
    private void handleSessionUnsubscribeEvent(final SessionUnsubscribeEvent event) {
    	final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		final String sessionId = headerAccessor.getSessionId();
		if (sessionId != null) {
			unsubscribeClient(sessionId);
		}
		logger.info(this.clientsRooms.toString());
    }
    
    /*
     * Removes the client from all the topics it was subscribed to.
     */
    private void unsubscribeClient(final String sessionId) {
    	this.clientsRooms.entrySet().forEach(room -> {
    		if (room.getValue().remove(sessionId)) {
    			// Checks the number of the remaining client connections subscribed to the same room
    			if (room.getValue().isEmpty()) {
    				// If the disconnected client was the last one, resets ticket counter
					this.ticketDispenserClient.resetTicket(room.getKey());
					logger.info("Ticket resetted for room " + room.getKey());
    			}
    		}
    	});
    }
	
}
