package it.unibo.springchat.utility;

import static java.lang.String.format;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Environment class for chat service constants.
 *
 */
public final class Consts {

	/**
	 * Key string for the session attribute related to the client's username.
	 */
	public static final String SESSION_USERNAME_ATTR = "username";
	
	/**
	 * Key string for the session attribute related to the room id in which the client is connected.
	 */
	public static final String SESSION_ROOM_ID_ATTR = "roomId";
	
	/**
	 * Key string for the session attribute related to the session id.
	 */
	public static final String SESSION_ID_ATTR = "sessionId";
	
	/**
	 * Starter key string for chat rooms' channels.
	 */
	private static final String TOPIC_ROOM_STARTER = "/topic/channel.";
	
	/**
	 * Gets the topic with the routing key for the specified room id.
	 * @param roomId
	 * 		the id of the room
	 * @return the topic string related to the room
	 */
	public static final String getTopic(final String roomId) {
		return format(TOPIC_ROOM_STARTER.concat("%s"), roomId);
	}
	
	/**
	 * Gets the room id from a certain topic.
	 * @param topic
	 * 		the topic from which extract the room id
	 * @return room id related to the specified topic
	 */
	public static final Optional<String> getRoomIdFromTopic(final String topic) {
		Objects.requireNonNull(topic);
		if (checkTopicMatching(topic)) {
			return Optional.of(topic.split("\\.")[1]);
		} else {
			return Optional.empty();
		}
	}
	
	private static boolean checkTopicMatching(final String topic) {
		final Pattern pattern = Pattern.compile("^(" + TOPIC_ROOM_STARTER + ")[a-zA-Z0-9]*");
		final Matcher matcher = pattern.matcher(topic);
		return matcher.find();
	}
	
}
