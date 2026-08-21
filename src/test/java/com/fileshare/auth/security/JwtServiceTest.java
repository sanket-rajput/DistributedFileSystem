package com.fileshare.auth.security;

import com.fileshare.user.entity.Role;
import com.fileshare.user.entity.User;
import com.fileshare.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Token generation produces a non-null, well-formed token for a mocked user")
    void generateToken_ProducesNonNullValidToken() {
        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .passwordHash("hashed_pass")
                .role(Role.USER)
                .build();

        UserPrincipal principal = UserPrincipal.create(mockUser);
        when(authentication.getPrincipal()).thenReturn(principal);

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // Standard JWT header.payload.signature format
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("user@example.com");
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Token validation succeeds for a valid token")
    void validateToken_ValidToken_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "valid@example.com", "pass", Role.USER, Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);

        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Token validation fails for an expired token")
    void validateToken_ExpiredToken_ReturnsFalse() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(TEST_SECRET, -5000L); // expired 5s ago
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "expired@example.com", "pass", Role.USER, Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);

        String expiredToken = expiredTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Token validation fails for a tampered token")
    void validateToken_TamperedToken_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "tampered@example.com", "pass", Role.USER, Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);

        String validToken = jwtTokenProvider.generateToken(authentication);
        String tamperedToken = validToken + "invalid_signature";

        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        assertThat(isValid).isFalse();
    }
}
