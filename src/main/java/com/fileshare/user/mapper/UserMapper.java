package com.fileshare.user.mapper;

import com.fileshare.user.dto.UserResponseDto;
import com.fileshare.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);
}
