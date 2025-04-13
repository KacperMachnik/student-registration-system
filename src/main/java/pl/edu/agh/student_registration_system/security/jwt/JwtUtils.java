package pl.edu.agh.student_registration_system.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${student_registration_system.jwtSecret}")
    private String jwtSecret;

    @Value("${student_registration_system.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${student_registration_system.jwtCookieName}")
    private String jwtCookieName;

    public String getJwtFromCookies(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if (cookie != null) {
            log.trace("JWT Cookie found: {}", cookie.getValue());
            return cookie.getValue();
        } else {
            log.trace("JWT Cookie not found");
            return null;
        }
    }


    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromIdentifier(userPrincipal.getUsername());
        return ResponseCookie.from(jwtCookieName, jwt)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .httpOnly(true)
                .secure(false) // true later?
                //.sameSite("Strict")
                .build();
    }


    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookieName, null).path("/").maxAge(0).build();
    }


    public String generateTokenFromIdentifier(String identifier) {
        return Jwts.builder()
                .subject(identifier)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }


    public String getIdentifierFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }
}