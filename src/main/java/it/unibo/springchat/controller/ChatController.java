package it.unibo.springchat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import it.unibo.springchat.config.Consts;
import it.unibo.springchat.model.ChatMessage;
import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.model.ConfigMessage;
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
		
		logger.info("Trying to send the message...");
		
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
		
	}

	@MessageMapping("/chat/{roomId}/addUser")
	public void addUser(@DestinationVariable final String roomId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) {

		// Retrieves the name of the new connected client
		final String username = chatMessage.getSender();
		
		// Saves user data into the related socket session
		headerAccessor.getSessionAttributes().put(Consts.SESSION_USERNAME, username);
		headerAccessor.getSessionAttributes().put(Consts.SESSION_ROOM_ID, roomId);
		
		// Sends a ordered join message to the clients connected in the same room
		final OrderedChatMessage joinMessage = new OrderedChatMessage(
				MessageType.JOIN,
				chatMessage.getSender(),
				this.ticketDispenserClient.getTicket(roomId));
		this.messagingTemplate.convertAndSend(Consts.getTopic(roomId), joinMessage);
		logger.info("User " + chatMessage.getSender() + " joined to the " + roomId + ". Sent a message to " + Consts.getTopic(roomId));
		
		// Sends a configuration message to the client with the current ticket number for the requested room
		this.messagingTemplate.convertAndSendToUser(
				headerAccessor.getSessionId(),
				"/queue/config",
				new ConfigMessage(this.ticketDispenserClient.countTickets(roomId)));
		
	}
}
