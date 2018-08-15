package it.unibo.springchat.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.unibo.springchat.controller.ExpirationListener;

@Configuration
@EnableTransactionManagement
public class RedisConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    
	@Bean
    public RedisConnectionFactory redisConnectionFactory() throws URISyntaxException {
        final RedisStandaloneConfiguration rc = new RedisStandaloneConfiguration();
        final URI redisUri = new  URI(System.getenv("REDIS_URL"));
        rc.setHostName(redisUri.getHost());
        rc.setPort(redisUri.getPort());
        rc.setDatabase(1);
        return new JedisConnectionFactory(rc);
    }

	@Primary
    @Bean
    public RedisTemplate<String, String> redisTemplate(final RedisConnectionFactory rcf){
        final RedisTemplate<String, String> rt = new RedisTemplate<String, String>();
        rt.setConnectionFactory(rcf);
        rt.setEnableTransactionSupport(true);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new StringRedisSerializer());
        return rt;
    }
	
    @Bean
    public RedisMessageListenerContainer keyExpirationListenerContainer(final RedisConnectionFactory connectionFactory,
    		final ExpirationListener expirationListener) {
        final RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.addMessageListener(expirationListener, new PatternTopic("__keyevent@1__:expired"));
        listenerContainer.setErrorHandler(e -> logger.error("There was an error in redis key expiration listener container", e));
        return listenerContainer;
    }
    
	/*
	@Bean
	public RedisMessageListenerContainer keyExpirationListenerContainer(final RedisConnectionFactory connectionFactory) {
	    final RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
	    listenerContainer.setConnectionFactory(connectionFactory);
	    listenerContainer.addMessageListener((message, pattern) -> {
	    	System.out.println("KEY EXPIRED");
	    }, new PatternTopic("__keyevent@*__:expired"));
	    return listenerContainer;
	}
	*/
    
}
