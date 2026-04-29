package com.helloclaudecode.service;

import com.helloclaudecode.dto.LoginRequest;
import com.helloclaudecode.dto.LoginResponse;
import com.helloclaudecode.entity.User;
import com.helloclaudecode.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtService.generate(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}
