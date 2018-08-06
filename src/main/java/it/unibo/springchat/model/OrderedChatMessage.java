package it.unibo.springchat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderedChatMessage extends ChatMessage {
	
	private long order;
	
	@JsonCreator
	public OrderedChatMessage(
			@JsonProperty("messageType") MessageType messageType,
			@JsonProperty("content") String content,
			@JsonProperty("sender") String sender,
			long order) {
		super(messageType, content, sender);
		this.order = order;
	}
	
	@JsonCreator
	public OrderedChatMessage(
			@JsonProperty("messageType") MessageType messageType,
			@JsonProperty("sender") String sender,
			long order) {
		super(messageType, sender);
		this.order = order;
	}
	
	public long getOrder() {
		return this.order;
	}
	
	public void setOrder(long order) {
		this.order = order;
	}
	
	@Override
    public String toString() {
        return "ChatMessage{" +
                "messageType='" + super.getMessageType().name() + '\'' +
                ", content='" + super.getContent() + '\'' +
                ", sender='" + super.getSender() + '\'' +
                ", order=" + this.order +
                '}';
    }

}
