package it.unibo.springchat.config;

import static java.lang.String.format;

import java.util.Objects;


/**
 * Environment class for chat service constants.
 *
 */
public final class Consts {

	/**
	 * Key string for the session attribute related to the client's username.
	 */
	public static final String SESSION_USERNAME = "username";
	
	/**
	 * Key string for the session attribute related to the room id in which the client is connected.
	 */
	public static final String SESSION_ROOM_ID = "room_id";
	
	/**
	 * Gets the topic with the routing key for the specified room id.
	 * @param roomId
	 * 		the id of the room
	 * @return the topic string related to the room
	 */
	public static final String getTopic(final String roomId) {
		return format("/topic/channel.%s", roomId);
	}
	
	/**
	 * Gets the room id from a certain topic.
	 * @param topic
	 * 		the topic from which extract the room id
	 * @return room id related to the specified topic
	 */
	public static final String getRoomIdFromTopic(final String topic) {
		Objects.requireNonNull(topic);
		return topic.split("\\.")[1];
	}
	
}
