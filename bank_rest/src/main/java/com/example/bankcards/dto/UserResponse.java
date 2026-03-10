package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        Boolean active,
        String userName,
        String fullName,
        String email,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static List<UserResponse> listFromEntity(List<User> users){
        return users.stream().map(UserResponse::fromEntity).toList();
    }

    public static UserResponse fromEntity(User user){
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
}
