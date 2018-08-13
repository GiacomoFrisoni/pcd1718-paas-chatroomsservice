package it.unibo.springchat.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class RedisConfig {
	
	@Bean
    public RedisConnectionFactory redisConnectionFactory() throws URISyntaxException {
        final RedisStandaloneConfiguration rc = new RedisStandaloneConfiguration();
        final URI redisUri = new  URI(System.getenv("REDIS_URL"));
        rc.setHostName(redisUri.getHost());
        rc.setPort(redisUri.getPort());
        return new JedisConnectionFactory(rc);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(final RedisConnectionFactory rcf){
        final RedisTemplate<String, String> rt = new RedisTemplate<String, String>();
        rt.setConnectionFactory(rcf);
        rt.setEnableTransactionSupport(true);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new StringRedisSerializer());
        return rt;
    }
    
}
