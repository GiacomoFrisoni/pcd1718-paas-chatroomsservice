package it.unibo.springchat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import it.unibo.springchat.model.ChatMessage.MessageType;
import it.unibo.springchat.model.OrderedChatMessage;
import it.unibo.springchat.utility.Consts;

@Component
public class ExpirationListener implements MessageListener {
	
    private static final Logger logger = LoggerFactory.getLogger(ExpirationListener.class);

    @Autowired
    private TicketDispenserClient ticketDispenserClient;
	
	@Autowired
	private SimpMessageSendingOperations messagingTemplate;
    
    @Override
    public void onMessage(final Message message, final byte[] bytes) {
    	
    	// Retrieves the id of the room related to the mutual exclusion expiration
        final String roomId = new String(message.getBody());
        
        // Retrieves the chat room topic
		final String topicRoom = Consts.getTopic(roomId);
        
		/*
		 * Retrieves a ticket from the ticket dispenser service and creates an ordered message.
		 * Since the mutual exclusion has been released due to the expiration of the associated timeout,
		 * there is no sender.
		 */
		final OrderedChatMessage orderedChatMessage = new OrderedChatMessage(
				MessageType.RELEASE_MUTEX,
				null,
				this.ticketDispenserClient.getTicket(roomId));

		// Sends the ordered message to the clients of the same room
		this.messagingTemplate.convertAndSend(topicRoom, orderedChatMessage);
		logger.info("Expired timeout for mutex acquiring. Sent a message to " + topicRoom);
		
    }
    
}