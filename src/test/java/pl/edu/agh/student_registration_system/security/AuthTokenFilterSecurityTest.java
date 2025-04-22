package pl.edu.agh.student_registration_system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import pl.edu.agh.student_registration_system.security.jwt.AuthTokenFilter;
import pl.edu.agh.student_registration_system.security.jwt.JwtUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsServiceImpl;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterSecurityTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private final String validToken = "valid.jwt.token";
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(securityContext);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void shouldContinueFilterChainWhenNoTokenPresent() throws Exception {
        when(jwtUtils.getJwtFromCookies(any())).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldContinueFilterChainWhenTokenIsInvalid() throws Exception {
        when(jwtUtils.getJwtFromCookies(any())).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {
        when(jwtUtils.getJwtFromCookies(any())).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getIdentifierFromJwtToken(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any());
    }

    @Test
    void shouldContinueFilterChainWhenExceptionOccurs() throws Exception {
        when(jwtUtils.getJwtFromCookies(any())).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getIdentifierFromJwtToken(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenThrow(new RuntimeException("Test exception"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldParseJwtFromRequest() throws Exception {
        when(jwtUtils.getJwtFromCookies(any())).thenReturn(validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getIdentifierFromJwtToken(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils).getJwtFromCookies(any());
    }
}
