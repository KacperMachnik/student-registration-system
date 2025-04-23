package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.agh.student_registration_system.exceptions.AuthenticationFailedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.repository.RoleRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;
import pl.edu.agh.student_registration_system.security.jwt.JwtUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserDetailsImpl userDetails;
    private LoginDTO loginDTO;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setRoleId(1L);
        role.setRoleName(RoleType.STUDENT);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRole(role);

        userDetails = new UserDetailsImpl(
                1L,
                "test@example.com",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.login(loginDTO);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getUsername());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(1, response.getRoles().size());
        assertEquals("ROLE_STUDENT", response.getRoles().get(0));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_ShouldThrowAuthenticationFailedException_WhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        assertThrows(AuthenticationFailedException.class, () -> authService.login(loginDTO));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getUserDetails_ShouldReturnLoginResponse_WhenUserIsAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.getUserDetails(authentication);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getUsername());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(1, response.getRoles().size());
        assertEquals("ROLE_STUDENT", response.getRoles().get(0));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserDetails_ShouldReturnNull_WhenUserIsNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        LoginResponse response = authService.getUserDetails(authentication);

        assertNull(response);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getUserDetails_ShouldReturnNull_WhenAuthenticationIsNull() {
        LoginResponse response = authService.getUserDetails(null);

        assertNull(response);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getUserDetails_ShouldReturnNull_WhenPrincipalIsAnonymousUser() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        LoginResponse response = authService.getUserDetails(authentication);

        assertNull(response);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getUserDetails_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getUserDetails(authentication));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void generateCookie_ShouldReturnResponseCookie() {
        ResponseCookie expectedCookie = ResponseCookie.from("jwt", "token").build();
        when(jwtUtils.generateJwtCookie(userDetails)).thenReturn(expectedCookie);

        ResponseCookie result = authService.generateCookie(userDetails);

        assertNotNull(result);
        assertEquals(expectedCookie, result);
        verify(jwtUtils).generateJwtCookie(userDetails);
    }

    @Test
    void getCleanJwtCookie_ShouldReturnCleanCookie() {
        ResponseCookie expectedCookie = ResponseCookie.from("jwt", "").build();
        when(jwtUtils.getCleanJwtCookie()).thenReturn(expectedCookie);

        ResponseCookie result = authService.getCleanJwtCookie();

        assertNotNull(result);
        assertEquals(expectedCookie, result);
        verify(jwtUtils).getCleanJwtCookie();
    }
}
