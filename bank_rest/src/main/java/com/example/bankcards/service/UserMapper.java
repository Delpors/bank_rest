package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMapper {

    public List<UserResponse> listFromEntity(List<User> users){
        return users.stream().map(this::toUserResponse).toList();
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getActive(),
                user.getUserName(),
                user.getFullName(),
                user.getEmail(),
                user.getRol(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
    public User tuEntity(UserRequest request){
        User user = new User();

        user.setUserName(request.userName());
        user.setPassword(request.password());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRol(request.role());
        return user;

    }
}
