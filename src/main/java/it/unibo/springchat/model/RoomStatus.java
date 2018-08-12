package it.unibo.springchat.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.gson.Gson;

public class RoomStatus implements Serializable {

	/**
	 * Auto-generated version UID.
	 */
	private static final long serialVersionUID = 4218804496744508304L;

	/*
	 * This data structure stores the client which have the mutex enabled on the chat.
	 * The Optional type released by Google is serializable.
	 */
	private Optional<String> clientWithMutex;
	
	/*
	 * This data structure keeps track of the session identifiers of the clients subscribed
	 * to the topic room.
	 */
	private final Set<String> subscribedClients;
	
	public RoomStatus() {
		this.clientWithMutex = Optional.absent();
		this.subscribedClients = new HashSet<>();
	}
	
	public Optional<String> getClientWithMutex() {
		return this.clientWithMutex;
	}
	
	public void setClientWithMutex(final String sessionId) {
		this.clientWithMutex = Optional.of(sessionId);
	}
	
	public void addSubscribedClient(final String sessionId) {
		this.subscribedClients.add(sessionId);
	}
	
	public boolean removeSubscribedClient(final String sessionId) {
		return this.subscribedClients.remove(sessionId);
	}
	
	public Set<String> getSubscribedClients() {
		return Collections.unmodifiableSet(this.subscribedClients);
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
	
	
}
