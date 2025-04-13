package pl.edu.agh.student_registration_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.repository.RoleRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

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
            if (request.getIndexNumber() == null || request.getIndexNumber().isBlank()) {
                throw new IllegalArgumentException("Index number is required for student registration.");
            }
            if (studentRepository.existsByIndexNumber(request.getIndexNumber())) {
                throw new UserAlreadyExistsException("Index number '" + request.getIndexNumber() + "' is already in use!");
            }
            Student student = new Student();
            student.setIndexNumber(request.getIndexNumber());
            student.setUser(user);
            user.setStudentProfile(student);
            log.info("Creating student profile for user email: {}", user.getEmail());

        } else if (request.getRoleType() == RoleType.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setTitle(request.getTitle());
            teacher.setUser(user);
            user.setTeacherProfile(teacher);
            log.info("Creating teacher profile for user email: {}", user.getEmail());
        } else {
            log.info("Creating DEANERY_STAFF user email: {}", user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("User with email {} registered successfully with role {} and ID {}",
                savedUser.getEmail(), savedUser.getRole().getRoleName(), savedUser.getUserId());
        return savedUser;
    }
}