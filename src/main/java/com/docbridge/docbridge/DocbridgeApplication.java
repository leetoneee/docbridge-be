package com.docbridge.docbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing // bật tự động set createdAt / updatedAt
@EnableAsync
public class DocbridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocbridgeApplication.class, args);
	}

}
