package com.fileshare.event.service;

import com.fileshare.event.config.KafkaConfig;
import com.fileshare.event.dto.FileDeletedEvent;
import com.fileshare.event.dto.FileSharedEvent;
import com.fileshare.event.dto.FileUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherServiceImpl implements EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherServiceImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final io.micrometer.core.instrument.Counter kafkaEventsPublishedCounter;

    public EventPublisherServiceImpl(KafkaTemplate<String, Object> kafkaTemplate,
                                  io.micrometer.core.instrument.Counter kafkaEventsPublishedCounter) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEventsPublishedCounter = kafkaEventsPublishedCounter;
    }

    @Override
    public void publishFileUploaded(FileUploadedEvent event) {
        publishEvent("FILE_UPLOADED", event.getFileId().toString(), event);
    }

    @Override
    public void publishFileShared(FileSharedEvent event) {
        publishEvent("FILE_SHARED", event.getFileId().toString(), event);
    }

    @Override
    public void publishFileDeleted(FileDeletedEvent event) {
        publishEvent("FILE_DELETED", event.getFileId().toString(), event);
    }

    private void publishEvent(String eventType, String key, Object payload) {
        try {
            kafkaTemplate.send(KafkaConfig.FILE_EVENTS_TOPIC, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            kafkaEventsPublishedCounter.increment();
                            log.info("Kafka event [{}] published successfully for key: {}", eventType, key);
                        } else {
                            log.error("Failed to publish Kafka event [{}]: {}", eventType, ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Fire-and-forget Kafka publish exception for [{}]: {}", eventType, e.getMessage());
        }
    }
}
