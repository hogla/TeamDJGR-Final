package se.djgr.sql;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JaxSqLprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaxSqLprojectApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run(ApplicationContext context) {
		return args -> {
		};
	}
}
