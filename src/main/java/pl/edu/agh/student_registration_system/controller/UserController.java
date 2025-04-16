package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.payload.response.MessageResponse;
import pl.edu.agh.student_registration_system.service.UserService;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDTO registerRequest) {
        log.info("Deanery staff attempting to register user with email: {}", registerRequest.getEmail());
        userService.registerNewUser(registerRequest);
        log.info("User with email {} registered successfully by Deanery staff.", registerRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully!"));
    }

    // TODO: Dodać inne endpointy zarządzania użytkownikami
}
