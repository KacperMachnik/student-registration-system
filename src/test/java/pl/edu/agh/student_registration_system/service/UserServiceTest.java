package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.agh.student_registration_system.exceptions.AuthenticationFailedException;
import pl.edu.agh.student_registration_system.exceptions.IndexNumberGenerationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.repository.RoleRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private UserDetails userDetails;


    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDTO studentRegisterDTO;
    private RegisterDTO teacherRegisterDTO;
    private RegisterDTO deaneryRegisterDTO;
    private Role studentRole;
    private Role teacherRole;
    private Role deaneryRole;

    @BeforeEach
    void setUp() {
        studentRegisterDTO = new RegisterDTO();
        studentRegisterDTO.setEmail("student@example.com");
        studentRegisterDTO.setPassword("password123");
        studentRegisterDTO.setFirstName("John");
        studentRegisterDTO.setLastName("Doe");
        studentRegisterDTO.setRoleType(RoleType.STUDENT);

        teacherRegisterDTO = new RegisterDTO();
        teacherRegisterDTO.setEmail("teacher@example.com");
        teacherRegisterDTO.setPassword("password123");
        teacherRegisterDTO.setFirstName("Jane");
        teacherRegisterDTO.setLastName("Smith");
        teacherRegisterDTO.setRoleType(RoleType.TEACHER);
        teacherRegisterDTO.setTitle("Dr.");

        deaneryRegisterDTO = new RegisterDTO();
        deaneryRegisterDTO.setEmail("deanery@example.com");
        deaneryRegisterDTO.setPassword("password123");
        deaneryRegisterDTO.setFirstName("Admin");
        deaneryRegisterDTO.setLastName("User");
        deaneryRegisterDTO.setRoleType(RoleType.DEANERY_STAFF);


        studentRole = new Role(1L, RoleType.STUDENT, null);
        teacherRole = new Role(2L, RoleType.TEACHER, null);
        deaneryRole = new Role(3L, RoleType.DEANERY_STAFF, null);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registerNewUser_Student_Success() {
        when(userRepository.existsByEmail(studentRegisterDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(studentRegisterDTO.getPassword())).thenReturn("encodedPassword");
        when(studentRepository.existsByIndexNumber(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            if (user.getStudentProfile() != null) {
                user.getStudentProfile().setStudentId(1L);
            }
            return user;
        });

        User registeredUser = userService.registerNewUser(studentRegisterDTO);

        assertNotNull(registeredUser);
        assertEquals(studentRegisterDTO.getEmail(), registeredUser.getEmail());
        assertNotNull(registeredUser.getStudentProfile());
        assertNotNull(registeredUser.getStudentProfile().getIndexNumber());
        verify(userRepository).save(any(User.class));
        verify(studentRepository, atLeastOnce()).existsByIndexNumber(anyString());
    }

    @Test
    void registerNewUser_Teacher_Success() {
        when(userRepository.existsByEmail(teacherRegisterDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.TEACHER)).thenReturn(Optional.of(teacherRole));
        when(passwordEncoder.encode(teacherRegisterDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            if (user.getTeacherProfile() != null) {
                user.getTeacherProfile().setTeacherId(1L);
            }
            return user;
        });

        User registeredUser = userService.registerNewUser(teacherRegisterDTO);

        assertNotNull(registeredUser);
        assertEquals(teacherRegisterDTO.getEmail(), registeredUser.getEmail());
        assertNotNull(registeredUser.getTeacherProfile());
        assertEquals(teacherRegisterDTO.getTitle(), registeredUser.getTeacherProfile().getTitle());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerNewUser_Teacher_TitleRequired_ThrowsIllegalArgumentException() {
        RegisterDTO teacherDtoNoTitle = new RegisterDTO();
        teacherDtoNoTitle.setEmail("teacher.notitle@example.com");
        teacherDtoNoTitle.setPassword("password123");
        teacherDtoNoTitle.setFirstName("No");
        teacherDtoNoTitle.setLastName("Title");
        teacherDtoNoTitle.setRoleType(RoleType.TEACHER);
        teacherDtoNoTitle.setTitle(" ");

        when(userRepository.existsByEmail(teacherDtoNoTitle.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.TEACHER)).thenReturn(Optional.of(teacherRole));

        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(teacherDtoNoTitle));
    }


    @Test
    void registerNewUser_DeaneryStaff_Success() {
        when(userRepository.existsByEmail(deaneryRegisterDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.DEANERY_STAFF)).thenReturn(Optional.of(deaneryRole));
        when(passwordEncoder.encode(deaneryRegisterDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });

        User registeredUser = userService.registerNewUser(deaneryRegisterDTO);

        assertNotNull(registeredUser);
        assertEquals(deaneryRegisterDTO.getEmail(), registeredUser.getEmail());
        assertNull(registeredUser.getStudentProfile());
        assertNull(registeredUser.getTeacherProfile());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerNewUser_EmailExists_ThrowsUserAlreadyExistsException() {
        when(userRepository.existsByEmail(studentRegisterDTO.getEmail())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerNewUser(studentRegisterDTO));
    }

    @Test
    void registerNewUser_RoleNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.existsByEmail(studentRegisterDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.STUDENT)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.registerNewUser(studentRegisterDTO));
    }

    @Test
    void registerNewUser_IndexGenerationFails_ThrowsIndexNumberGenerationException() {
        when(userRepository.existsByEmail(studentRegisterDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(studentRegisterDTO.getPassword())).thenReturn("encodedPassword");
        when(studentRepository.existsByIndexNumber(anyString())).thenReturn(true);

        assertThrows(IndexNumberGenerationException.class, () -> userService.registerNewUser(studentRegisterDTO));
    }

    @Test
    void getCurrentAuthenticatedUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmailWithRole("test@example.com")).thenReturn(Optional.of(user));

        User currentUser = userService.getCurrentAuthenticatedUser();

        assertNotNull(currentUser);
        assertEquals("test@example.com", currentUser.getEmail());
    }

    @Test
    void getCurrentAuthenticatedUser_NotAuthenticated_ThrowsAuthenticationFailedException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertThrows(AuthenticationFailedException.class, () -> userService.getCurrentAuthenticatedUser());
    }

    @Test
    void getCurrentAuthenticatedUser_AnonymousUser_ThrowsAuthenticationFailedException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        assertThrows(AuthenticationFailedException.class, () -> userService.getCurrentAuthenticatedUser());
    }


    @Test
    void getCurrentAuthenticatedUser_UserNotFoundInDb_ThrowsUsernameNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmailWithRole("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getCurrentAuthenticatedUser());
    }

    @Test
    void getCurrentAuthenticatedUser_UnexpectedPrincipalType_ThrowsIllegalStateException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new Object());

        assertThrows(IllegalStateException.class, () -> userService.getCurrentAuthenticatedUser());
    }

}