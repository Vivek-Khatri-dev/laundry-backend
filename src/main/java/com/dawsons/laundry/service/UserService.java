package com.dawsons.laundry.service;

import com.dawsons.laundry.controller.SapApiController;
import com.dawsons.laundry.dto.UserCreateRequest;
import com.dawsons.laundry.entity.Role;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.exception.BadRequestException;
import com.dawsons.laundry.exception.ResourceNotFoundException;
import com.dawsons.laundry.repository.RoleRepository;
import com.dawsons.laundry.repository.UserRepository;
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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
        try {
            // Make sure both roles exist. Role name must be the BARE name
            // ("ADMIN"/"CASHIER") because UserPrincipal.getAuthorities()
            // already prepends "ROLE_" — storing "ROLE_ADMIN" here would
            // produce the authority "ROLE_ROLE_ADMIN" and break hasRole("ADMIN").
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        logger.info("Creating ADMIN role...");
                        Role newRole = new Role();
                        newRole.setName("ADMIN");
                        return roleRepository.save(newRole);
                    });

            roleRepository.findByName("CASHIER")
                    .orElseGet(() -> {
                        logger.info("Creating CASHIER role...");
                        Role newRole = new Role();
                        newRole.setName("CASHIER");
                        return roleRepository.save(newRole);
                    });

            // Check if admin user exists
            Optional<User> existingAdmin = userRepository.findByUsername("admin");

            if (existingAdmin.isEmpty()) {
                String seedPassword = System.getenv().getOrDefault("SEED_ADMIN_PASSWORD", "Admin@123");
                logger.info("Creating admin user...");
                User admin = new User();
                admin.setFullName("System Administrator");
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode(seedPassword));
                admin.setRole(adminRole);
                admin.setActive(true);
                userRepository.save(admin);
                logger.info("✅ Admin user created successfully! Username: admin");
            } else {
                logger.info("Admin user already exists.");
            }

        } catch (Exception e) {
            logger.error("❌ Error creating system user: {}", e.getMessage());
            // Don't crash the application
        }
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }
}