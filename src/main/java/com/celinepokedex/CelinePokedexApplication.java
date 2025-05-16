package com.celinepokedex;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CelinePokedexApplication {

	public static void main(String[] args) {
		// Load .env file variables before starting the application
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		
		// Create Spring application
		SpringApplication app = new SpringApplication(CelinePokedexApplication.class);
		
		// Add a hook to inject environment variables
		app.addInitializers(applicationContext -> {
			ConfigurableEnvironment environment = applicationContext.getEnvironment();
			Map<String, Object> envMap = new HashMap<>();
			
			// Add all .env variables to Spring environment
			dotenv.entries().forEach(entry -> 
				envMap.put(entry.getKey(), entry.getValue())
			);
			
			MapPropertySource propertySource = new MapPropertySource("dotenvProperties", envMap);
			environment.getPropertySources().addFirst(propertySource);
		});
		
		app.run(args);
	}
}
