package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request){
        authenticationManager.authenticate
                (new UsernamePasswordAuthenticationToken
                        (request.userName(), request.password()));

        User user = userRepository.findByUserName(request.userName())
                .orElseThrow(
                        ()-> new UsernameNotFoundException
                                (String.format("Пользователь с именем %s, не найден", request.userName())));

        String token = jwtService.generateToken(new UserPrincipal(user));

        return ResponseEntity.ok
                (new AuthResponse(
                        token,
                        user.getUserName(),
                        user.getFullName(),
                        user.getRol().toString()
                ));
    }

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        boolean isFirstUser = userRepository.count() == 0;
        UserRole role = isFirstUser ? UserRole.ADMIN: UserRole.USER;

        User user = new User(
                request.userName(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                request.email(),
                role,
                null
        );

        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return ResponseEntity.ok(
                new AuthResponse(
                        token,
                        user.getUserName(),
                        user.getFullName(),
                        user.getRol().toString()
                )
        );
    }
}
