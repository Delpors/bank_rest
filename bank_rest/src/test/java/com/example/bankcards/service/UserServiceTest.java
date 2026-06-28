package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponse userResponse;
    private UserRequest userRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .userName("john_doe")
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .rol(UserRole.USER)
                .active(true)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userResponse = new UserResponse(
                1L,
                true,
                "john_doe",
                "John Doe",
                "john@example.com",
                UserRole.USER,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        userRequest = new UserRequest(
                "john_doe",
                "password123",
                "John Doe",
                "john@example.com",
                UserRole.USER,
                1L
        );

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllUsers_shouldReturnPageOfUserResponses() {
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAllByActiveTrue(pageable)).thenReturn(userPage);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(userResponse);

        verify(userRepository).findAllByActiveTrue(pageable);
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void createUser_shouldMapRequestToEntityAndReturnResponse() {
        when(userMapper.tuEntity(userRequest)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userRequest);

        assertThat(result).isEqualTo(userResponse);
        verify(userMapper).tuEntity(userRequest);
        verify(userMapper).toUserResponse(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    void blockUser_whenUserActive_shouldSetActiveFalseAndReturnResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.blockUser(1L);

        assertThat(result).isEqualTo(userResponse);
        assertThat(user.isActive()).isFalse();
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponse(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void blockUser_whenUserAlreadyBlocked_shouldLogAndReturnResponseWithoutChanges() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.blockUser(1L);

        assertThat(result).isEqualTo(userResponse);
        assertThat(user.isActive()).isFalse(); // остаётся false
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponse(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void activateUser_whenUserInactive_shouldSetActiveTrueAndReturnResponse() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.activateUser(1L);

        assertThat(result).isEqualTo(userResponse);
        assertThat(user.isActive()).isTrue();
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponse(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void activateUser_whenUserAlreadyActive_shouldLogAndReturnResponseWithoutChanges() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.activateUser(1L);

        assertThat(result).isEqualTo(userResponse);
        assertThat(user.isActive()).isTrue();
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponse(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_whenUserExistsAndNotDeleted_shouldSetDeletedTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_whenUserAlreadyDeleted_shouldLogAndReturnWithoutChanges() {
        user.setDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с id 1 не найден!");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getUserById(1L);

        assertThat(found).isEqualTo(user);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с id 1 не найден");

        verify(userRepository).findById(1L);
    }
}