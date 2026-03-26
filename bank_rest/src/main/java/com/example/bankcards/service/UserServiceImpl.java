package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
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
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse blockUser(Long userId) {

        User user = getUserById(userId);

        if (!user.isActive()){
            log.info("Пользователь с id {} уже заблокирована!", userId);
            return userMapper.toUserResponse(user);
        }

        user.setActive(false);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse activateUser(Long userId) {

        User user = getUserById(userId);

        if (user.isActive()){
            log.info("Пользователь с id {} уже активен!", userId);
            return userMapper.toUserResponse(user);
        }

        user.setActive(true);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(@NotNull Long userId) {

        User existUser = userRepository
                .findById(userId)
                .orElseThrow(()-> new UserNotFoundException(String.format("Пользователь с id %d не найден!", userId)));

        if (existUser.isDeleted()){
            log.info("Пользователь с id {}, был удален ранее", userId);
            return;
        }

        existUser.setDeleted(true);
    }

    public User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));
    }
}
