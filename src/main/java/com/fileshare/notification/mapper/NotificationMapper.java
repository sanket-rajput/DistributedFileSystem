package com.fileshare.notification.mapper;

import com.fileshare.notification.dto.NotificationResponseDto;
import com.fileshare.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    NotificationResponseDto toDto(Notification notification);
}
