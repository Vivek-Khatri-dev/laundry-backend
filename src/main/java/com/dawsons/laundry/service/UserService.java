package com.dawsons.laundry.service;

import com.dawsons.laundry.controller.SapApiController;
import com.dawsons.laundry.dto.UserCreateRequest;
import com.dawsons.laundry.entity.Role;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.exception.BadRequestException;
import com.dawsons.laundry.exception.ResourceNotFoundException;
import com.dawsons.laundry.repository.RoleRepository;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private static final Logger logger = LoggerFactory.getLogger(SapApiController.class);

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User createUser(UserCreateRequest request, User actor) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BadRequestException("Unknown role: " + request.getRole()));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        User saved = userRepository.save(user);

        auditService.log(actor, "CREATE_USER", "USER", saved.getId(),
                "Created user '" + saved.getUsername() + "' with role " + role.getName());

        return saved;
    }

    /** Deactivate instead of delete - preserves the audit trail's user references. */
    public User deactivate(Integer id, User actor) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User #" + id + " not found"));

        user.setActive(false);
        User saved = userRepository.save(user);

        auditService.log(actor, "DEACTIVATE_USER", "USER", saved.getId(),
                "Deactivated user '" + saved.getUsername() + "'");
        return saved;
    }

    public User reactivate(Integer id, User actor) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User #" + id + " not found"));

        user.setActive(true);
        User saved = userRepository.save(user);

        auditService.log(actor, "REACTIVATE_USER", "USER", saved.getId(),
                "Reactivated user '" + saved.getUsername() + "'");
        return saved;
    }

    @PostConstruct
    public void createSystemUser() {
        if (!userRepository.findByUsername("system").isPresent()) {
            User systemUser = new User();
            systemUser.setFullName("System User");
            systemUser.setUsername("system");
            systemUser.setPasswordHash(passwordEncoder.encode("system123"));
            systemUser.setRole(roleRepository.findByName("ADMIN").get());
            systemUser.setActive(true);
            userRepository.save(systemUser);
            logger.info("System user created successfully");
        }
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }
}
