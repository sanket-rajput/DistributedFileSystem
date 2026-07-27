package com.fileshare.auth;

import com.fileshare.auth.dto.AuthResponse;
import com.fileshare.auth.dto.LoginRequest;
import com.fileshare.auth.dto.RegisterRequest;
import com.fileshare.auth.security.JwtTokenProvider;
import com.fileshare.auth.security.UserPrincipal;
import com.fileshare.auth.service.AuthServiceImpl;
import com.fileshare.common.exception.DuplicateResourceException;
import com.fileshare.user.dto.UserResponseDto;
import com.fileshare.user.entity.Role;
import com.fileshare.user.entity.User;
import com.fileshare.user.mapper.UserMapper;
import com.fileshare.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;

    private JwtTokenProvider tokenProvider;
    private AuthServiceImpl authService;

    private User sampleUser;
    private UserResponseDto sampleUserDto;
    private Authentication sampleAuth;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 86400000L);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, tokenProvider, authenticationManager, userMapper);

        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPass")
                .role(Role.USER)
                .build();

        sampleUserDto = UserResponseDto.builder()
                .id(sampleUser.getId())
                .email(sampleUser.getEmail())
                .role(sampleUser.getRole())
                .build();

        UserPrincipal principal = UserPrincipal.create(sampleUser);
        sampleAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", Role.USER);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(sampleAuth);
        when(userMapper.toDto(sampleUser)).thenReturn(sampleUserDto);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", Role.USER);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(sampleAuth);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(userMapper.toDto(sampleUser)).thenReturn(sampleUserDto);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
    }
}
