package com.fileshare.notification.service;

import com.fileshare.notification.dto.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    Page<NotificationResponseDto> getUserNotifications(UUID userId, Pageable pageable);
}
