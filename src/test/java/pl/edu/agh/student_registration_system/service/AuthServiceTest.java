package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.edu.agh.student_registration_system.exceptions.AuthenticationFailedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.repository.UserRepository;
import pl.edu.agh.student_registration_system.security.jwt.JwtUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

import java.util.Collections;
import java.util.List;
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

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UserDetailsImpl userDetails;
    private LoginDTO loginDTO;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        Role studentRole = new Role(1L, RoleType.STUDENT, Collections.emptySet());
        user = new User(1L, "John", "Doe", "password", "john.doe@example.com", true, studentRole, null, null);

        userDetails = new UserDetailsImpl(1L, "john.doe@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.STUDENT.name())));

        loginDTO = new LoginDTO();
        loginDTO.setEmail("john.doe@example.com");
        loginDTO.setPassword("password");

        authentication = mock(Authentication.class);
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));

        LoginResponse loginResponse = authService.login(loginDTO);

        assertNotNull(loginResponse);
        assertEquals(user.getUserId(), loginResponse.getId());
        assertEquals(user.getEmail(), loginResponse.getUsername());
        assertEquals(user.getFirstName(), loginResponse.getFirstName());
        assertEquals(user.getLastName(), loginResponse.getLastName());
        assertEquals(1, loginResponse.getRoles().size());
        assertEquals(RoleType.STUDENT.name(), loginResponse.getRoles().get(0));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void login_AuthenticationFailed_ThrowsAuthenticationFailedException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationFailedException.class, () -> authService.login(loginDTO));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void login_UserNotFoundAfterAuthentication_ThrowsResourceNotFoundException() {
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginDTO));
    }

    @Test
    void getUserDetails_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));

        LoginResponse loginResponse = authService.getUserDetails(authentication);

        assertNotNull(loginResponse);
        assertEquals(user.getUserId(), loginResponse.getId());
        assertEquals(user.getEmail(), loginResponse.getUsername());
    }

    @Test
    void getUserDetails_NullAuthentication_ReturnsNull() {
        LoginResponse loginResponse = authService.getUserDetails(null);
        assertNull(loginResponse);
    }

    @Test
    void getUserDetails_NotAuthenticated_ReturnsNull() {
        when(authentication.isAuthenticated()).thenReturn(false);
        LoginResponse loginResponse = authService.getUserDetails(authentication);
        assertNull(loginResponse);
    }

    @Test
    void getUserDetails_AnonymousUser_ReturnsNull() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        LoginResponse loginResponse = authService.getUserDetails(authentication);
        assertNull(loginResponse);
    }

    @Test
    void getUserDetails_UserNotFoundInRepository_ThrowsResourceNotFoundException() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getUserDetails(authentication));
    }


    @Test
    void generateCookie_Success() {
        ResponseCookie mockCookie = ResponseCookie.from("jwt", "token").build();
        when(jwtUtils.generateJwtCookie(userDetails)).thenReturn(mockCookie);

        ResponseCookie resultCookie = authService.generateCookie(userDetails);

        assertNotNull(resultCookie);
        assertEquals("jwt", resultCookie.getName());
        assertEquals("token", resultCookie.getValue());
        verify(jwtUtils, times(1)).generateJwtCookie(userDetails);
    }

    @Test
    void getCleanJwtCookie_Success() {
        ResponseCookie mockCookie = ResponseCookie.from("jwt", "").maxAge(0).build();
        when(jwtUtils.getCleanJwtCookie()).thenReturn(mockCookie);

        ResponseCookie resultCookie = authService.getCleanJwtCookie();

        assertNotNull(resultCookie);
        assertEquals("jwt", resultCookie.getName());
        assertTrue(resultCookie.getValue().isEmpty());
        assertEquals(0, resultCookie.getMaxAge().getSeconds());
        verify(jwtUtils, times(1)).getCleanJwtCookie();
    }
}