package it.unibo.springchat.controller;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import it.unibo.springchat.model.ChatMessage;
import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.model.OrderedChatMessage;

@Controller
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	
	@Autowired
    private TicketDispenserClient ticketDispenserClient;
	
	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	@MessageMapping("/chat/{roomId}/sendMessage")
	public void sendMessage(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage) {
		
		// Retrieves a ticket from the ticket dispenser service and creates and ordered message
		final OrderedChatMessage orderedChatMessage = new OrderedChatMessage(
				chatMessage.getType(),
				chatMessage.getContent(),
				chatMessage.getSender(),
				this.ticketDispenserClient.getTicket(roomId));
        
		// Sends the ordered message to the clients of the same room
		this.messagingTemplate.convertAndSend(format("/topic/channel.%s", roomId), orderedChatMessage);
		
	}

	@MessageMapping("/chat/{roomId}/addUser")
	public void addUser(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) {
		
		// Saves user data into the related socket session
		final String username = chatMessage.getSender();
		headerAccessor.getSessionAttributes().put("username", username);
		headerAccessor.getSessionAttributes().put("room_id", roomId);
		
		// Checks data
		if (username != null && roomId != null) {
			// Sends a ordered join message to the clients connected in the same room
			final OrderedChatMessage joinMessage = new OrderedChatMessage(
					MessageType.JOIN,
					chatMessage.getSender(),
					this.ticketDispenserClient.getTicket(roomId));
			this.messagingTemplate.convertAndSend(format("/topic/channel.%s", roomId), joinMessage);
			logger.info("User " + chatMessage.getSender() + " joined to the " + roomId + ". Sent a message to " + format("/topic/channel.%s", roomId));
		}
		
	}
}
