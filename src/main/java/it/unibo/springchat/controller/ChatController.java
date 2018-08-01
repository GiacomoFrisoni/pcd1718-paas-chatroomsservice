package it.unibo.springchat.controller;

import static java.lang.String.format;

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

	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	@MessageMapping("/chat/{roomId}/sendMessage")
	public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
		// TODO: Transforms to ordered chat messages
		messagingTemplate.convertAndSend(format("/channel/%s", roomId), chatMessage);
	}

	@MessageMapping("/chat/{roomId}/addUser")
	public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		final String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);
		if (currentRoomId != null) {
			final ChatMessage leaveMessage = new ChatMessage();
			leaveMessage.setType(MessageType.JOIN);
			leaveMessage.setSender(chatMessage.getSender());
			messagingTemplate.convertAndSend(format("/channel/%s", currentRoomId), leaveMessage);
		}
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		messagingTemplate.convertAndSend(format("/channel/%s", roomId), chatMessage);
	}
}
