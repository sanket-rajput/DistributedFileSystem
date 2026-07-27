package com.fileshare.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileshare.event.config.KafkaConfig;
import com.fileshare.notification.entity.Notification;
import com.fileshare.notification.repository.NotificationRepository;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final io.micrometer.core.instrument.Counter kafkaEventsConsumedCounter;

    public NotificationConsumer(NotificationRepository notificationRepository,
                                UserRepository userRepository,
                                ObjectMapper objectMapper,
                                io.micrometer.core.instrument.Counter kafkaEventsConsumedCounter) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.kafkaEventsConsumedCounter = kafkaEventsConsumedCounter;
    }

    @KafkaListener(topics = KafkaConfig.FILE_EVENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id:fileshare-notification-group}")
    public void consumeFileEvent(Object message) {
        try {
            kafkaEventsConsumedCounter.increment();
            log.info("Received Kafka event message: {}", message);
            Map<?, ?> map = objectMapper.convertValue(message, Map.class);

            if (map.containsKey("userId")) {
                UUID userId = UUID.fromString((String) map.get("userId"));
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    String eventType = "FILE_EVENT";
                    String text = "File event triggered for file ID: " + map.get("fileId");

                    if (map.containsKey("filename") && map.containsKey("sizeBytes")) {
                        eventType = "FILE_UPLOAD";
                        text = String.format("File '%s' (%d bytes) was uploaded successfully.", map.get("filename"), map.get("sizeBytes"));
                    } else if (map.containsKey("shareToken")) {
                        eventType = "FILE_SHARE";
                        text = String.format("A share link was generated for file ID %s.", map.get("fileId"));
                    } else if (map.containsKey("filename")) {
                        eventType = "FILE_DELETE";
                        text = String.format("File '%s' was deleted.", map.get("filename"));
                    }

                    Notification notification = Notification.builder()
                            .user(user)
                            .message(text)
                            .type(eventType)
                            .read(false)
                            .build();

                    notificationRepository.save(notification);
                    log.info("Persisted Notification ID {} for User {}", notification.getId(), userId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process consumed Kafka event: {}", e.getMessage(), e);
        }
    }
}
