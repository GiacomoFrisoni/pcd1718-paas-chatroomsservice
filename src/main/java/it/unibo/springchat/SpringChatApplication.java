package it.unibo.springchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@RefreshScope
@EnableTransactionManagement
public class SpringChatApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SpringChatApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringChatApplication.class, args);
	}
	
}
