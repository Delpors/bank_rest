package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<UserResponse> getAllUsers(Pageable pageable)
    {
        log.info("Получить список всех пользователей");

        Page<User> users = userRepository.findAllByActiveTrue(pageable);
        return users.map(userMapper::toUserResponse);
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {

        log.info("Попытка создать пользователя {}",userRequest.userName());

        User user = userMapper.tuEntity(userRequest);
        log.info("пользователь {}, успешно сохранен в базу данных",userRequest.userName());
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse blockUser(Long userId) {

        log.info("Попытка заблокировать пользователя с id: {}",userId);
        User user = getUserById(userId);

        if (!user.isActive()){
            log.info("Пользователь с id {} уже заблокирована!", userId);
            return userMapper.toUserResponse(user);
        }

        user.setActive(false);
        log.info("Пользователя с id: {}, успешно заблокирован.",userId);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse activateUser(Long userId) {

        log.info("Попытка активировать пользователя с id: {}",userId);
        User user = getUserById(userId);

        if (user.isActive()){
            log.info("Пользователь с id {} уже активен!", userId);
            return userMapper.toUserResponse(user);
        }

        user.setActive(true);
        log.info("Пользователя с id: {}, успешно активирован.",userId);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(@NotNull Long userId) {

        log.info("Попытка удалить пользователя с id: {}",userId);
        User existUser = userRepository
                .findById(userId)
                .orElseThrow(()-> new UserNotFoundException(String.format("Пользователь с id %d не найден!", userId)));

        if (existUser.isDeleted()){
            log.info("Пользователь с id {}, был удален ранее", userId);
            return;
        }

        existUser.setDeleted(true);
        log.info("Пользователя с id: {}, удален.",userId);
    }

    public User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException
                        (String.format("Пользователь с id %s не найден", userId)));
    }
}
