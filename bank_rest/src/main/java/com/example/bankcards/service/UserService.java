package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<UserResponse> getAllUsers(Pageable pageable)
    {
        Page<User> users = userRepository.findAllByActiveTrue(pageable);
        return users.map(userMapper::toUserResponse);
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        User user = userMapper.tuEntity(userRequest);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));

        user.setActive(false);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));

        user.setActive(true);
        return userMapper.toUserResponse(userRepository.save(user));
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
