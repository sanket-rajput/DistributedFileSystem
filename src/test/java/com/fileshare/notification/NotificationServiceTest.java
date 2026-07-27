package com.fileshare.notification;

import com.fileshare.notification.dto.NotificationResponseDto;
import com.fileshare.notification.entity.Notification;
import com.fileshare.notification.mapper.NotificationMapper;
import com.fileshare.notification.repository.NotificationRepository;
import com.fileshare.notification.service.NotificationServiceImpl;
import com.fileshare.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID userId;
    private Notification notification;
    private NotificationResponseDto notificationDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("user@example.com").build();
        notification = Notification.builder().id(UUID.randomUUID()).user(user).message("File uploaded").type("FILE_UPLOAD").build();
        notificationDto = NotificationResponseDto.builder().id(notification.getId()).message("File uploaded").type("FILE_UPLOAD").build();
    }

    @Test
    void getUserNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(page);
        when(notificationMapper.toDto(notification)).thenReturn(notificationDto);

        Page<NotificationResponseDto> result = notificationService.getUserNotifications(userId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("File uploaded", result.getContent().get(0).getMessage());
    }
}
