package com.docbridge.docbridge;

import com.docbridge.docbridge.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"spring.elasticsearch.enabled=false",
		"spring.data.elasticsearch.repositories.enabled=false"
})
@Import(TestConfig.class)
@AutoConfigureMockMvc
class DocbridgeApplicationTests {

	@Test
	void contextLoads() {
	}

}
