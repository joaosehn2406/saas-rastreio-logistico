package com.jps.jps;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class JpsApplicationTests {

	@Test
	void applicationEntryPointIsConfigured() {
		if (!JpsApplication.class.isAnnotationPresent(SpringBootApplication.class)) {
			throw new AssertionError("JpsApplication must be annotated with SpringBootApplication");
		}
	}

}
