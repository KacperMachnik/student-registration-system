package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.payload.response.MessageResponse;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;
import pl.edu.agh.student_registration_system.service.AuthService;


@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginRequest) {
        log.info("Login attempt for user email: {}", loginRequest.getEmail());
        LoginResponse loginResponse = authService.login(loginRequest);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = authService.generateCookie(userDetails);

        log.info("User with email '{}' authenticated successfully. Setting JWT cookie.", loginRequest.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        log.info("Logout request received.");
        ResponseCookie cookie = authService.getCleanJwtCookie();
        log.info("User logged out. Clearing JWT cookie.");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out successfully!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        log.debug("Request to get current user details.");
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempt to get /me for non-authenticated user.");
            return ResponseEntity.status(401).body(new MessageResponse("Error: User not authenticated"));
        }
        LoginResponse response = authService.getUserDetails(authentication);
        if (response == null || response.getId() == null) {
            String identifier = (authentication.getPrincipal() instanceof UserDetails)
                    ? ((UserDetails) authentication.getPrincipal()).getUsername()
                    : authentication.getName();
            log.error("Could not retrieve user details for authenticated user: {}", identifier);
            return ResponseEntity.internalServerError().body(new MessageResponse("Error: Could not retrieve user details"));
        }
        log.debug("Returning details for user: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }
}