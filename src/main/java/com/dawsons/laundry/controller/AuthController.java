package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.LoginRequest;
import com.dawsons.laundry.dto.LoginResponse;
import com.dawsons.laundry.security.JwtUtil;
import com.dawsons.laundry.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        var authToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        var authentication = authenticationManager.authenticate(authToken); // throws BadCredentialsException on failure

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String token = jwtUtil.generateToken(
                principal.getUsername(), principal.getRoleName(), principal.getId(), principal.getFullName());

        return ResponseEntity.ok(new LoginResponse(
                token, principal.getId(), principal.getFullName(), principal.getUsername(), principal.getRoleName()));
    }
}
