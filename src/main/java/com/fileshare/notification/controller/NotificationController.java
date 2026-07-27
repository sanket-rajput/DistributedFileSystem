package com.fileshare.notification.controller;

import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.common.dto.ApiResponse;
import com.fileshare.notification.dto.NotificationResponseDto;
import com.fileshare.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification System", description = "Endpoints for retrieving user notifications generated asynchronously via Kafka events")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Returns a paginated list of notifications generated for the logged-in user.")
    public ResponseEntity<ApiResponse<Page<NotificationResponseDto>>> getUserNotifications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDto> response = notificationService.getUserNotifications(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Notifications retrieved successfully"));
    }
}
