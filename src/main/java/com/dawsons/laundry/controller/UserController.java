package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.PasswordChangeRequest;
import com.dawsons.laundry.dto.UserCreateRequest;
import com.dawsons.laundry.dto.UserResponse;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.security.UserPrincipal;
import com.dawsons.laundry.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Admin only endpoints under /api/users
    @GetMapping("/users")
    public List<UserResponse> getAll() {
        return userService.getAll().stream().map(UserResponse::new).collect(Collectors.toList());
    }

    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return new UserResponse(userService.createUser(request, currentUser(principal)));
    }

    @PostMapping("/users/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return new UserResponse(userService.deactivate(id, currentUser(principal)));
    }

    @PostMapping("/users/{id}/reactivate")
    public UserResponse reactivate(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        return new UserResponse(userService.reactivate(id, currentUser(principal)));
    }

    // Both roles can change their own password - moved to /api/user (singular)
    @PostMapping("/user/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }
        
        // Update to new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        return ResponseEntity.ok("Password changed successfully");
    }

    private User currentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished from DB"));
    }
}