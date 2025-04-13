package pl.edu.agh.student_registration_system.service;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

public interface AuthService {
    LoginResponse login(LoginDTO loginDTO);

    LoginResponse getUserDetails(Authentication authentication);

    ResponseCookie generateCookie(UserDetailsImpl userDetails);

    ResponseCookie getCleanJwtCookie();
}