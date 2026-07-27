package com.fileshare.config.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter kafkaEventsPublishedCounter(MeterRegistry registry) {
        return Counter.builder("kafka.events.published")
                .description("Number of Kafka events published by the application")
                .register(registry);
    }

    @Bean
    public Counter kafkaEventsConsumedCounter(MeterRegistry registry) {
        return Counter.builder("kafka.events.consumed")
                .description("Number of Kafka events consumed by the application")
                .register(registry);
    }

    @Bean
    public Counter deduplicatedUploadsCounter(MeterRegistry registry) {
        return Counter.builder("file.upload.deduplicated")
                .description("Number of file uploads deduplicated via SHA-256 matching")
                .register(registry);
    }

    @Bean
    public Timer fileUploadTimer(MeterRegistry registry) {
        return Timer.builder("file.upload.time")
                .description("Time taken to process and stream file uploads")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer fileDownloadTimer(MeterRegistry registry) {
        return Timer.builder("file.download.time")
                .description("Time taken to stream file downloads")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
