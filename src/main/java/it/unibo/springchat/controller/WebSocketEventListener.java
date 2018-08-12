package it.unibo.springchat.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.model.OrderedChatMessage;
import it.unibo.springchat.model.RoomStatus;
import it.unibo.springchat.utility.Consts;

@Component
public class WebSocketEventListener {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	
    @Autowired
    private RedisTemplate<String, RoomStatus> redis;

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
		final String sessionId = (String) headerAccessor.getSessionAttributes().get(Consts.SESSION_ID_ATTR);
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
		logger.info("SESSION ID: " + sessionId);
		logger.info("DESTINATION: " + headerAccessor.getDestination());
		final Optional<String> roomId = Consts.getRoomIdFromTopic(headerAccessor.getDestination());
		logger.info("OPTIONAL ROOM ID: " + roomId);
		if (sessionId != null && roomId != null && roomId.isPresent()) {
			logger.info("REDIS: " + this.redis);/*
			this.redis.execute(new SessionCallback<List<Object>>() {
	        	@SuppressWarnings({ "rawtypes", "unchecked" })
	            @Override
	            public List<Object> execute(final RedisOperations ops) throws DataAccessException {
            		logger.info("OPS: " + ops);
	                ops.multi();
            		logger.info("ROOM ID: " + roomId.get());
	                ops.opsForValue().setIfAbsent(roomId.get(), new RoomStatus());
            		logger.info("CI ARRIVA");
            		logger.info("ROOM STATUS: " + ops.opsForValue().get(roomId.get()));
	    			((RoomStatus) ops.opsForValue().get(roomId.get())).addSubscribedClient(sessionId);
	                return ops.exec();
	            }
	        });*/
			this.redis.opsForValue().setIfAbsent(roomId.get(), new RoomStatus());
			this.redis.opsForValue().get(roomId.get()).addSubscribedClient(sessionId);
		}
    }

    @EventListener
    private void handleSessionUnsubscribeEvent(final SessionUnsubscribeEvent event) {
    	final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		final String sessionId = headerAccessor.getSessionId();
		if (sessionId != null) {
			unsubscribeClient(sessionId);
		}
    }
    
    /*
     * Removes the client from all the topics it was subscribed to.
     */
    private void unsubscribeClient(final String sessionId) {
    	
    	redis.execute(new SessionCallback<List<Object>>() {
        	@SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public List<Object> execute(final RedisOperations ops) throws DataAccessException {
                ops.multi();
                ops.keys("*").forEach(room -> {
                	if (((RoomStatus) ops.opsForValue().get(room)).removeSubscribedClient(sessionId)) {
                		// Checks the number of the remaining client connections subscribed to the same room
                		if (((RoomStatus) ops.opsForValue().get(room)).getSubscribedClients().isEmpty()) {
                			// If the disconnected client was the last one, resets ticket counter
                			ticketDispenserClient.resetTicket((String) room);
        					logger.info("Ticket resetted for room " + room);
                    	}
                	}
                });
                return ops.exec();
            }
        });
    	
    }
	
}
