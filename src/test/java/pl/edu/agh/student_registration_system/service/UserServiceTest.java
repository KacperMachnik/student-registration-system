package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.agh.student_registration_system.exceptions.AuthenticationFailedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.repository.RoleRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegisterDTO registerDTO;
    private Role studentRole;
    private Role teacherRole;
    private User testUser;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setRoleId(1L);
        studentRole.setRoleName(RoleType.STUDENT);

        teacherRole = new Role();
        teacherRole.setRoleId(2L);
        teacherRole.setRoleName(RoleType.TEACHER);

        registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("password123");
        registerDTO.setFirstName("John");
        registerDTO.setLastName("Doe");
        registerDTO.setRoleType(RoleType.STUDENT);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRole(studentRole);

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
        }
    }

    @Test
    void registerNewUser_ShouldRegisterStudent_WhenRoleIsStudent() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(studentRepository.existsByIndexNumber(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerNewUser(registerDTO);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getIsActive());
        assertEquals(RoleType.STUDENT, result.getRole().getRoleName());

        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository).findByRoleName(RoleType.STUDENT);
        verify(passwordEncoder).encode("password123");
        verify(studentRepository).existsByIndexNumber(anyString());
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getStudentProfile());
        assertNotNull(capturedUser.getStudentProfile().getIndexNumber());
    }

    @Test
    void registerNewUser_ShouldRegisterTeacher_WhenRoleIsTeacher() {
        registerDTO.setRoleType(RoleType.TEACHER);
        registerDTO.setTitle("Professor");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.TEACHER)).thenReturn(Optional.of(teacherRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerNewUser(registerDTO);

        assertNotNull(result);
        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository).findByRoleName(RoleType.TEACHER);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getTeacherProfile());
        assertEquals("Professor", capturedUser.getTeacherProfile().getTitle());
    }

    @Test
    void registerNewUser_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerNewUser(registerDTO));

        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository, never()).findByRoleName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerNewUser_ShouldThrowResourceNotFoundException_WhenRoleNotFound() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.STUDENT)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.registerNewUser(registerDTO));

        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository).findByRoleName(RoleType.STUDENT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerNewUser_ShouldThrowIllegalArgumentException_WhenTeacherWithoutTitle() {
        registerDTO.setRoleType(RoleType.TEACHER);
        registerDTO.setTitle(null);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleType.TEACHER)).thenReturn(Optional.of(teacherRole));

        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(registerDTO));

        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository).findByRoleName(RoleType.TEACHER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getCurrentAuthenticatedUser_ShouldReturnUser_WhenUserIsAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmailWithRole("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.getCurrentAuthenticatedUser();

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication, atLeastOnce()).getPrincipal();
        verify(userDetails).getUsername();
        verify(userRepository).findByEmailWithRole("test@example.com");
    }

    @Test
    void getCurrentAuthenticatedUser_ShouldThrowAuthenticationFailedException_WhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AuthenticationFailedException.class, () -> userService.getCurrentAuthenticatedUser());

        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(userRepository, never()).findByEmailWithRole(anyString());
    }

    @Test
    void getCurrentAuthenticatedUser_ShouldThrowAuthenticationFailedException_WhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AuthenticationFailedException.class, () -> userService.getCurrentAuthenticatedUser());

        verify(securityContext).getAuthentication();
        verify(userRepository, never()).findByEmailWithRole(anyString());
    }

    @Test
    void getCurrentAuthenticatedUser_ShouldThrowAuthenticationFailedException_WhenPrincipalIsAnonymousUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        assertThrows(AuthenticationFailedException.class, () -> userService.getCurrentAuthenticatedUser());

        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication, atLeastOnce()).getPrincipal();
        verify(userRepository, never()).findByEmailWithRole(anyString());
    }

    @Test
    void getCurrentAuthenticatedUser_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmailWithRole("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getCurrentAuthenticatedUser());

        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication, atLeastOnce()).getPrincipal();
        verify(userDetails).getUsername();
        verify(userRepository).findByEmailWithRole("test@example.com");
    }
}
