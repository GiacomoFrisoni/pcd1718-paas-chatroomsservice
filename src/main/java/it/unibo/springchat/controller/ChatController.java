package it.unibo.springchat.controller;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import it.unibo.springchat.model.ChatMessage;
import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.utility.Consts;
import it.unibo.springchat.model.ConfigMessage;
import it.unibo.springchat.model.OrderedChatMessage;

@Controller
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
	
	@Autowired
    private TicketDispenserClient ticketDispenserClient;
	
	@Autowired
	private SimpMessageSendingOperations messagingTemplate;
	
    @Autowired
    private RedisTemplate<String, String> redis;

    @MessageMapping("/chat/{roomId}/addUser")
	@SendToUser("/queue/config")
	public ConfigMessage addUser(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) throws Exception {
		
		// Retrieves the name of the new connected client
		final String username = chatMessage.getSender();
		
		// Saves user data into the related socket session
		headerAccessor.getSessionAttributes().put(Consts.SESSION_USERNAME_ATTR, username);
		headerAccessor.getSessionAttributes().put(Consts.SESSION_ROOM_ID_ATTR, roomId);
		
		// Sends a ordered join message to the clients connected in the same room
		final OrderedChatMessage joinMessage = new OrderedChatMessage(
				MessageType.JOIN,
				username,
				this.ticketDispenserClient.getTicket(roomId));
		this.messagingTemplate.convertAndSend(Consts.getTopic(roomId), joinMessage);
		logger.info("User " + username + " joined to " + roomId + ". Sent a message to " + Consts.getTopic(roomId));
		
		// Sends a configuration message to the client with the current ticket number for the requested room
		return new ConfigMessage(this.ticketDispenserClient.countTickets(roomId));
		
	}
    
	@MessageMapping("/chat/{roomId}/sendMessage")
	public void sendMessage(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) {
		
		logger.info("Trying to send the message...");
		
		/*
		 * Sends a message only if nobody has the mutual exclusion
		 * or only if the client is the one who has entered into the critical section.
		 */
		final Optional<String> clientWithMutex = Optional.ofNullable(redis.opsForValue().get(roomId));
		logger.info(clientWithMutex.toString());
		if (!clientWithMutex.isPresent()
				|| clientWithMutex.get().equals(headerAccessor.getSessionAttributes().get(Consts.SESSION_ID_ATTR))) {
			
			// Retrieves a ticket from the ticket dispenser service and creates and ordered message
			final OrderedChatMessage orderedChatMessage = new OrderedChatMessage(
					chatMessage.getMessageType(),
					chatMessage.getContent(),
					chatMessage.getSender(),
					this.ticketDispenserClient.getTicket(roomId));
	        
			// Sends the ordered message to the clients of the same room
			this.messagingTemplate.convertAndSend(Consts.getTopic(roomId), orderedChatMessage);
			
			logger.info("Message '" + orderedChatMessage.getContent() + "'\n"
					+ "from '" + orderedChatMessage.getSender() + "'\n"
					+ "was send to '" + Consts.getTopic(roomId) + "'");
			
		} else {
			logger.info("Mutex acquired by another client");
		}
		
	}
	
	@MessageMapping("/chat/{roomId}/getMutex")
	public void getMutex(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) {

		logger.info("SI PUO FAREEEEEEEEE");
		
		// Registers the new client with mutual exclusion only if the critical section is not already occupied
		if (redis.opsForValue().setIfAbsent(
				roomId,
				(String) headerAccessor.getSessionAttributes().get(Consts.SESSION_ID_ATTR))) {

			logger.info("NESSUNO AVEVA LA MUTEX");
			
			// Retrieves the name of the new connected client
			final String username = chatMessage.getSender();
			final String topicRoom = Consts.getTopic(roomId);
			
			// Retrieves a ticket from the ticket dispenser service and creates and ordered message
			final OrderedChatMessage orderedChatMessage = new OrderedChatMessage(
					MessageType.GET_MUTEX,
					username,
					this.ticketDispenserClient.getTicket(roomId));

			// Sends the ordered message to the clients of the same room
			this.messagingTemplate.convertAndSend(topicRoom, orderedChatMessage);
			logger.info("User " + username + " has got the mutex into " + roomId + ". Sent a message to " + topicRoom);
			
			// Sets a timeout for the mutual exclusion acquiring
			redis.expire(roomId, Consts.MUTEX_TMAX, TimeUnit.MILLISECONDS);
			
		} else {
			logger.info("Mutex not available");
		}
		
	}
	
	@MessageMapping("/chat/{roomId}/releaseMutex")
	public void releaseMutex(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) {
		
		// Checks if the client is the one who has the mutual exclusion
		if (redis.opsForValue().get(roomId).equals(
				(String) headerAccessor.getSessionAttributes().get(Consts.SESSION_ID_ATTR))) {
			
			// Releases the mutual exclusion
			redis.delete(roomId);
			
			// Retrieves the name of the new connected client
			final String username = chatMessage.getSender();
			final String topicRoom = Consts.getTopic(roomId);
			
			// Retrieves a ticket from the ticket dispenser service and creates and ordered message
			final OrderedChatMessage orderedChatMessage = new OrderedChatMessage(
					MessageType.RELEASE_MUTEX,
					username,
					this.ticketDispenserClient.getTicket(roomId));
			
			// Sends the ordered message to the clients of the same room
			this.messagingTemplate.convertAndSend(topicRoom, orderedChatMessage);
			logger.info("User " + username + " has released the mutex from " + roomId + ". Sent a message to " + topicRoom);
			
		}
		
	}
	
	@MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(final Throwable exception) {
        return exception.getMessage();
    }
	
}
