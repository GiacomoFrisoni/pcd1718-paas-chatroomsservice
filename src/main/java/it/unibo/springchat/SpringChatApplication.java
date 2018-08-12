package it.unibo.springchat;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import it.unibo.springchat.model.RoomStatus;

@SpringBootApplication
@RefreshScope
@EnableEurekaClient
@EnableFeignClients
@EnableTransactionManagement
public class SpringChatApplication extends SpringBootServletInitializer {
	
	@Bean
    public RedisConnectionFactory redisConnectionFactory() throws URISyntaxException {
        final RedisStandaloneConfiguration rc = new RedisStandaloneConfiguration();
        final URI redisUri = new  URI(System.getenv("REDIS_URL"));
        rc.setHostName(redisUri.getHost());
        rc.setPort(redisUri.getPort());
        return new JedisConnectionFactory(rc);
    }

    @Bean
    public RedisTemplate<String, RoomStatus> redisTemplate(final RedisConnectionFactory rcf){
        final RedisTemplate<String, RoomStatus> rt = new RedisTemplate<String, RoomStatus>();
        rt.setConnectionFactory(rcf);
        rt.setEnableTransactionSupport(true);
        rt.setKeySerializer(new StringRedisSerializer());
        //rt.setValueSerializer(new RoomStatusRedisSerializer());
        rt.setValueSerializer(new GenericToStringSerializer<>(RoomStatus.class));
        return rt;
    }
	
	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
		return builder.sources(SpringChatApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringChatApplication.class, args);
	}
	
}
