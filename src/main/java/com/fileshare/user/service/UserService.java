package com.fileshare.user.service;

import com.fileshare.user.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto getUserById(UUID id);

    UserResponseDto getUserByEmail(String email);

    List<UserResponseDto> getAllUsers();
}
