package pl.edu.agh.student_registration_system.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.agh.student_registration_system.security.jwt.JwtUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsSecurityTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsImpl userDetails;

    private final String jwtSecret = "testSecretKeyWithAtLeast256BitsToSatisfyHmacShaKeyRequirements";
    private final int jwtExpirationMs = 86400000; // 1 day
    private final String jwtCookieName = "testJwtCookie";
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);
        ReflectionTestUtils.setField(jwtUtils, "jwtCookieName", jwtCookieName);
    }

    @Test
    void shouldGetJwtFromCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie(jwtCookieName, "testJwtToken");
        request.setCookies(cookie);

        String token = jwtUtils.getJwtFromCookies(request);

        assertEquals("testJwtToken", token);
    }

    @Test
    void shouldReturnNullWhenNoCookieFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = jwtUtils.getJwtFromCookies(request);

        assertNull(token);
    }

    @Test
    void shouldReturnNullWhenCookieWithDifferentNameFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie("differentCookieName", "testJwtToken");
        request.setCookies(cookie);

        String token = jwtUtils.getJwtFromCookies(request);

        assertNull(token);
    }

    @Test
    void shouldGenerateJwtCookie() {
        when(userDetails.getUsername()).thenReturn(userEmail);

        ResponseCookie cookie = jwtUtils.generateJwtCookie(userDetails);

        assertNotNull(cookie);
        assertEquals(jwtCookieName, cookie.getName());
        assertNotNull(cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(jwtExpirationMs / 1000, cookie.getMaxAge().getSeconds());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.isSecure());
    }

    @Test
    void shouldGetCleanJwtCookie() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        assertNotNull(cookie);
        assertEquals(jwtCookieName, cookie.getName());
        
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }

    @Test
    void shouldGenerateTokenFromIdentifier() {
        String token = jwtUtils.generateTokenFromIdentifier(userEmail);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void shouldGetIdentifierFromJwtToken() {
        String token = jwtUtils.generateTokenFromIdentifier(userEmail);
        String extractedIdentifier = jwtUtils.getIdentifierFromJwtToken(token);

        assertEquals(userEmail, extractedIdentifier);
    }

    @Test
    void shouldValidateValidJwtToken() {
        String token = jwtUtils.generateTokenFromIdentifier(userEmail);
        boolean isValid = jwtUtils.validateJwtToken(token);

        assertTrue(isValid);
    }

    @Test
    void shouldNotValidateInvalidJwtToken() {
        boolean isValid = jwtUtils.validateJwtToken("invalid.jwt.token");

        assertFalse(isValid);
    }

    @Test
    void shouldNotValidateEmptyJwtToken() {
        boolean isValid = jwtUtils.validateJwtToken("");

        assertFalse(isValid);
    }

    @Test
    void shouldNotValidateNullJwtToken() {
        boolean isValid = jwtUtils.validateJwtToken(null);

        assertFalse(isValid);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtils.generateTokenFromIdentifier(userEmail);
        boolean isValid = jwtUtils.validateJwtToken(token);
        String extractedIdentifier = jwtUtils.getIdentifierFromJwtToken(token);

        assertTrue(isValid);
        assertEquals(userEmail, extractedIdentifier);
    }
}
