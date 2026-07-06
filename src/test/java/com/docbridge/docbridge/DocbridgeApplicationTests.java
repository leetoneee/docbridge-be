package com.docbridge.docbridge;

import com.docbridge.docbridge.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
class DocbridgeApplicationTests {

	@Test
	void contextLoads() {
	}

}
