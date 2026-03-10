package com.example.bankcards.controller;

import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final CardService cardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(){

        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<UserResponse> createUser(@RequestBody User user){
        UserResponse response = userService.createUser(user);

        return ResponseEntity
                .created(URI.create("/api/admin/users" + response.id()))
                .body(response);
    }

    @PutMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<UserResponse> blockUser(@PathVariable Long userId){

        return ResponseEntity.ok().body(userService.blockUser(userId));
    }

    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long userId){

        return ResponseEntity.ok().body(userService.activateUser(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
