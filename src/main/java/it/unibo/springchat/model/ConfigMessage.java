package it.unibo.springchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigMessage {
	
	private Long ticketsCount;
	
	public ConfigMessage(Long ticketsCount) {
		this.ticketsCount = ticketsCount;
	}

	public Long getTicketsCount() {
		return this.ticketsCount;
	}

	public void setTicketsCount(Long ticketsCount) {
		this.ticketsCount = ticketsCount;
	}

}
