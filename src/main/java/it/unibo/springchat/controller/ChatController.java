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

@Controller
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	
	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	@MessageMapping("/chat/{roomId}/sendMessage")
	public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
		// TODO: Transforms to ordered chat messages
		messagingTemplate.convertAndSend(format("/topic/%s", roomId), chatMessage);
	}

	@MessageMapping("/chat/{roomId}/addUser")
	public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("room_id", roomId);
		logger.info("User " + chatMessage.getSender() + " is trying to get the " + roomId);
		if (roomId != null) {
			final ChatMessage leaveMessage = new ChatMessage();
			leaveMessage.setType(MessageType.JOIN);
			leaveMessage.setSender(chatMessage.getSender());
			messagingTemplate.convertAndSend(format("/topic/%s", roomId), leaveMessage);
			logger.info("User " + chatMessage.getSender() + " joined to the " + roomId + ". Sent a message to " + format("/topic/%s", roomId));
		}
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		messagingTemplate.convertAndSend(format("/topic/%s", roomId), chatMessage);
	}
}
