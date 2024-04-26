package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
class ShareItTests {

	@Test
	void contextLoads() {
		ConfigurableEnvironment environment = new MockEnvironment();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.setEnvironment(environment);
		context.register(ShareItApp.class);
		context.refresh();
		assertNotNull(context.getBean(ShareItApp.class));
		context.close();
	}

}
