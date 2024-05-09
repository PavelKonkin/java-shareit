package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ShareItServerTest {

	@Test
	void contextLoads() {
		ConfigurableEnvironment environment = new MockEnvironment();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setEnvironment(environment);
		context.register(ShareItServer.class);
		context.refresh();
		assertNotNull(context.getBean(ShareItServer.class));
		context.close();
	}

}
