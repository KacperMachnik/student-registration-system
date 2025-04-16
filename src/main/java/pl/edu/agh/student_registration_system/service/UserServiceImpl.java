package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.AuthenticationFailedException;
import pl.edu.agh.student_registration_system.exceptions.IndexNumberGenerationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.repository.RoleRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int MAX_INDEX_GENERATION_ATTEMPTS = 15;

    @Override
    @Transactional
    public User registerNewUser(RegisterDTO request) {
        log.info("Attempting to register new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email [{}] is already in use!", request.getEmail());
            throw new UserAlreadyExistsException("Email '" + request.getEmail() + "' is already in use!");
        }

        Role userRole = roleRepository.findByRoleName(request.getRoleType())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "roleName", request.getRoleType().name()));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(userRole);
        user.setIsActive(true);

        if (request.getRoleType() == RoleType.STUDENT) {
            String uniqueIndexNumber = generateUniqueIndexNumber();
            log.info("Generated unique index number {} for student with email {}", uniqueIndexNumber, user.getEmail());

            Student student = new Student();
            student.setIndexNumber(uniqueIndexNumber);
            student.setUser(user);
            user.setStudentProfile(student);
            log.info("Creating student profile for user email: {}", user.getEmail());

        } else if (request.getRoleType() == RoleType.TEACHER) {
            Teacher teacher = new Teacher();
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                log.warn("Teacher registration failed for email {}: Title is required.", request.getEmail());
                throw new IllegalArgumentException("Academic title is required for teacher registration.");
            }
            teacher.setTitle(request.getTitle());
            teacher.setUser(user);
            user.setTeacherProfile(teacher);
            log.info("Creating teacher profile for user email: {}", user.getEmail());
        } else {
            log.info("Creating DEANERY_STAFF user with email: {}", user.getEmail());
        }


        User savedUser = userRepository.save(user);
        log.info("User with email {} registered successfully with role {} and ID {}",
                savedUser.getEmail(), savedUser.getRole().getRoleName(), savedUser.getUserId());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempted to get current user, but no user is authenticated.");
            throw new AuthenticationFailedException("User not authenticated");
        }

        String userIdentifier;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            userIdentifier = ((UserDetails) principal).getUsername();
            log.debug("Authenticated user identifier from UserDetails: {}", userIdentifier);
        } else {
            log.error("Unexpected principal type: {}", principal.getClass().getName());
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }

        return userRepository.findByEmailWithRole(userIdentifier)
                .orElseThrow(() -> {
                    log.error("Authenticated user '{}' not found in the database.", userIdentifier);
                    return new UsernameNotFoundException("User not found with identifier: " + userIdentifier);
                });
    }

    private String generateUniqueIndexNumber() {
        String indexNumber;
        int attempts = 0;
        do {
            if (attempts >= MAX_INDEX_GENERATION_ATTEMPTS) {
                log.error("Failed to generate a unique index number after {} attempts.", attempts);
                throw new IndexNumberGenerationException("Could not generate a unique index number. Please try again later or contact support.");
            }
            int randomNum = ThreadLocalRandom.current().nextInt(100000, 1000000);
            indexNumber = String.valueOf(randomNum);
            attempts++;
        } while (studentRepository.existsByIndexNumber(indexNumber));

        return indexNumber;
    }
}