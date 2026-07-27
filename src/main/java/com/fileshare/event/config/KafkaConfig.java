package com.fileshare.event.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String FILE_EVENTS_TOPIC = "file-events";

    @Bean
    public NewTopic fileEventsTopic() {
        return TopicBuilder.name(FILE_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
