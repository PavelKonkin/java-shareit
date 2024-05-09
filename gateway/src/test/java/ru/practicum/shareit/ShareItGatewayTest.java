package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ShareItGatewayTest {

	@Test
	void contextLoads() {
		ConfigurableEnvironment environment = new MockEnvironment();
		MutablePropertySources propertySources = environment.getPropertySources();
		propertySources.addLast(new MapPropertySource("testProperties",
				Collections.singletonMap("shareit-server.url", "http://localhost:9090")));

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setEnvironment(environment);
		context.register(ShareItGateway.class);
		context.refresh();
		assertNotNull(context.getBean(ShareItGateway.class));
		context.close();
	}
}
