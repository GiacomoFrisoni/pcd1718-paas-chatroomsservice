package it.unibo.springchat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import it.unibo.springchat.model.OrderedChatMessage;
import it.unibo.springchat.model.ChatMessage.MessageType;

@Component
public class ExpirationListener implements MessageListener {
	
    private static final Logger logger = LoggerFactory.getLogger(ExpirationListener.class);

    @Override
    public void onMessage(final Message message, final byte[] bytes) {
        final String key = new String(message.getBody());
        logger.debug("expired key: {}", key);
        /*
        // Retrieves a ticket from the ticket dispenser service and creates and ordered message
		final OrderedChatMessage orderedChatMessage = new OrderedChatMessage(
				MessageType.GET_MUTEX,
				username,
				this.ticketDispenserClient.getTicket(roomId));

		// Sends the ordered message to the clients of the same room
		this.messagingTemplate.convertAndSend(topicRoom, orderedChatMessage);
		logger.info("User " + username + " has got the mutex into " + roomId + ". Sent a message to " + topicRoom);
		*/
    }
    
}