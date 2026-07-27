package com.fileshare.user;

import com.fileshare.common.exception.ResourceNotFoundException;
import com.fileshare.user.dto.UserResponseDto;
import com.fileshare.user.entity.Role;
import com.fileshare.user.entity.User;
import com.fileshare.user.mapper.UserMapper;
import com.fileshare.user.repository.UserRepository;
import com.fileshare.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UserResponseDto sampleUserDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .role(Role.USER)
                .build();

        sampleUserDto = UserResponseDto.builder()
                .id(userId)
                .email("user@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userMapper.toDto(sampleUser)).thenReturn(sampleUserDto);

        UserResponseDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));
        when(userMapper.toDto(sampleUser)).thenReturn(sampleUserDto);

        List<UserResponseDto> results = userService.getAllUsers();

        assertEquals(1, results.size());
    }
}
