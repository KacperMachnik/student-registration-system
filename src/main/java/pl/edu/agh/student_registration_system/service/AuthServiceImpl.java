package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.edu.agh.student_registration_system.exceptions.AuthenticationFailedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.repository.UserRepository;
import pl.edu.agh.student_registration_system.security.jwt.JwtUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;



    @Override
    public LoginResponse login(LoginDTO loginDTO) {
        log.debug("Attempting login for user with email: {}", loginDTO.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow(
                    () -> new ResourceNotFoundException("User", "email", userDetails.getEmail()));

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            log.info("User with email '{}' logged in successfully with role: {}", loginDTO.getEmail(), roles.getFirst());
            return new LoginResponse(userDetails.getId(), userDetails.getUsername(), user.getFirstName(), user.getLastName(), user.getIsActive(), roles);

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {} - Reason: {}", loginDTO.getEmail(), e.getMessage());
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    @Override
    public LoginResponse getUserDetails(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempted to get user details for non-authenticated request");
            return null;
        }
        log.debug("Fetching user details for authenticated user");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", userDetails.getEmail()));
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        log.debug("User details fetched successfully for user email: {}", userDetails.getUsername());
        return new LoginResponse(userDetails.getId(), userDetails.getUsername(), user.getFirstName(), user.getLastName(), user.getIsActive(), roles);
    }

    @Override
    public ResponseCookie generateCookie(UserDetailsImpl userDetails) {
        log.debug("Generating JWT cookie for user email: {}", userDetails.getUsername());
        return jwtUtils.generateJwtCookie(userDetails);
    }

    @Override
    public ResponseCookie getCleanJwtCookie() {
        log.debug("Generating clean JWT cookie for logout");
        return jwtUtils.getCleanJwtCookie();
    }
}