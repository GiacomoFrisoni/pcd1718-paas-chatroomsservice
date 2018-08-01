package it.unibo.springchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

	public enum MessageType {
		CHAT, JOIN, LEAVE
	}

	private MessageType messageType;
	private String content;
	private String sender;
	
	public ChatMessage() { }
	
	public ChatMessage(MessageType messageType, String content, String sender) {
		this.messageType = messageType;
		this.content = content;
		this.sender = sender;
	}

	public MessageType getType() {
		return this.messageType;
	}

	public void setType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSender() {
		return this.sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
	
	@Override
    public String toString() {
        return "ChatMessage{" +
                "messageType='" + this.messageType.name() + '\'' +
                ", content='" + this.content + '\'' +
                ", sender='" + this.sender + '\'' +
                '}';
    }
	
}
