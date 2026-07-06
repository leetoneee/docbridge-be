package com.docbridge.docbridge.shared.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.docbridge.docbridge.module.log.audit"
)
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.repositories.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ElasticsearchConfig {
    @Bean
    ElasticsearchClient elasticsearchClient(
            RestClient restClient,
            ObjectMapper objectMapper) {

        ElasticsearchTransport transport =
                new RestClientTransport(
                        restClient,
                        new JacksonJsonpMapper(objectMapper)
                );

        return new ElasticsearchClient(transport);
    }
}
