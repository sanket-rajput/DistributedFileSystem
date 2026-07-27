package com.fileshare.auth.service;

import com.fileshare.auth.dto.AuthResponse;
import com.fileshare.auth.dto.LoginRequest;
import com.fileshare.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
