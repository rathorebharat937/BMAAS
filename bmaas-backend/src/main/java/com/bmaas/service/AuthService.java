package com.bmaas.service;

import com.bmaas.dto.auth.AuthResponse;
import com.bmaas.dto.auth.LoginRequest;
import com.bmaas.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
