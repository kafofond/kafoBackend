package kafofond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class KafobackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafobackendApplication.class, args);
	}

}
