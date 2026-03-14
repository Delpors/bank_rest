package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotBlank String userName,
        @NotBlank String password,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotNull UserRole role,
        @NotNull Long userId
) {
    public static User tuEntity(UserRequest request){
        User user = new User();

        user.setUserName(request.userName);
        user.setPassword(request.password);
        user.setFullName(request.fullName);
        user.setEmail(request.email);
        user.setRol(request.role);
        return user;

    }
}
