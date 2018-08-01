package it.unibo.springchat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderedChatMessage extends ChatMessage {
	
	private int order;
	
	@JsonCreator
	public OrderedChatMessage(
			@JsonProperty("messageType") MessageType messageType,
			@JsonProperty("content") String content,
			@JsonProperty("sender") String sender,
			int order) {
		super(messageType, content, sender);
		this.order = order;
	}
	
	public int getOrder() {
		return this.order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	@Override
    public String toString() {
        return "ChatMessage{" +
                "messageType='" + super.getType().name() + '\'' +
                ", content='" + super.getContent() + '\'' +
                ", sender='" + super.getSender() + '\'' +
                ", order=" + this.order +
                '}';
    }

}
