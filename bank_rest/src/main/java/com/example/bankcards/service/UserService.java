package com.example.bankcards.service;

import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllUsers() {
        return UserResponse.listFromEntity(userRepository.findAll());
    }

    @Transactional
    public UserResponse createUser(User user) {
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponse blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));

        user.setActive(false);

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));

        user.setActive(true);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));

        user.softDelete();
        userRepository.save(user);
    }
}
