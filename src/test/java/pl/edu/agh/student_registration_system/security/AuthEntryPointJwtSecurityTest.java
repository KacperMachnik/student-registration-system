package pl.edu.agh.student_registration_system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import pl.edu.agh.student_registration_system.security.jwt.AuthEntryPointJwt;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtSecurityTest {

    private AuthEntryPointJwt authEntryPointJwt;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        authEntryPointJwt = new AuthEntryPointJwt();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = new BadCredentialsException("Invalid credentials");
    }

    @Test
    void shouldSetProperResponseStatusAndContentType() throws IOException {
        request.setServletPath("/api/auth/signin");
        authEntryPointJwt.commence(request, response, authException);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json", response.getContentType());
    }

    @Test
    void shouldIncludeCorrectErrorDetailsInResponse() throws IOException {
        request.setServletPath("/api/auth/signin");
        authEntryPointJwt.commence(request, response, authException);
        String responseBody = response.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, responseMap.get("status"));
        assertEquals("Unauthorized", responseMap.get("error"));
        assertEquals("Invalid credentials", responseMap.get("message"));
        assertEquals("/api/auth/signin", responseMap.get("path"));
    }

    @Test
    void shouldHandleDifferentAuthenticationExceptions() throws IOException {
        request.setServletPath("/api/protected");
        AuthenticationException differentException = new AuthenticationException("Access denied") {};
        authEntryPointJwt.commence(request, response, differentException);
        String responseBody = response.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, responseMap.get("status"));
        assertEquals("Access denied", responseMap.get("message"));
    }

    @Test
    void shouldHandleDifferentRequestPaths() throws IOException {
        request.setServletPath("/api/students/123");
        authEntryPointJwt.commence(request, response, authException);
        String responseBody = response.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

        assertEquals("/api/students/123", responseMap.get("path"));
    }

    @Test
    void shouldWriteToResponseOutputStream() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        mockRequest.setServletPath("/api/test");

        authEntryPointJwt.commence(mockRequest, mockResponse, authException);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("application/json", mockResponse.getContentType());

        String responseBody = mockResponse.getContentAsString();
        assertFalse(responseBody.isEmpty());
        assertTrue(responseBody.contains("Unauthorized"));
    }
}
