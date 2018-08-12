package it.unibo.springchat.utility;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.google.gson.Gson;

import it.unibo.springchat.model.RoomStatus;

public class RoomStatusRedisSerializer implements RedisSerializer<RoomStatus> {

	private final Gson gson = new Gson();
	
	@Override
	public byte[] serialize(final RoomStatus roomStatus) throws SerializationException {
		System.out.println("SERIALIZATION: " + gson.toJson(roomStatus).toString());
		System.out.println("SERIALIZATION BYTES: " + gson.toJson(roomStatus).toString().getBytes());
		return (roomStatus != null ? gson.toJson(roomStatus).toString().getBytes() : new byte[0]);
	}

	@Override
	public RoomStatus deserialize(final byte[] bytes) throws SerializationException {
		if (bytes != null && bytes.length > 0) {
			final String jsonString = new String(bytes);
			System.out.println("DESERIALIZATION: " + jsonString);
			return gson.fromJson(jsonString, RoomStatus.class);
		} else {
			return null;
		}
	}

}
