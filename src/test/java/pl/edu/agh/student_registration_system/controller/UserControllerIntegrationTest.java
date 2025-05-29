package pl.edu.agh.student_registration_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.payload.response.MessageResponse;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    private final String ADMIN_EMAIL = "admin@university.com";
    private final String ADMIN_PASSWORD = "admin123";
    private final String JWT_COOKIE_NAME = "student-registration";

    private Cookie loginAsAdminAndGetCookie() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(ADMIN_EMAIL);
        loginDTO.setPassword(ADMIN_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseContent, LoginResponse.class);
        assertThat(loginResponse.getUsername()).isEqualTo(ADMIN_EMAIL);
        assertThat(loginResponse.getRoles()).contains(RoleType.DEANERY_STAFF.name());


        Cookie jwtCookie = loginResult.getResponse().getCookie(JWT_COOKIE_NAME);
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isNotBlank();
        return jwtCookie;
    }

    @Test
    void deaneryStaffShouldRegisterNewStudentSuccessfully() throws Exception {
        Cookie adminJwtCookie = loginAsAdminAndGetCookie();

        RegisterDTO registerDTO = new RegisterDTO();
        String newStudentEmail = "new.student.test@example.com";
        registerDTO.setEmail(newStudentEmail);
        registerDTO.setPassword("password123");
        registerDTO.setFirstName("Jan");
        registerDTO.setLastName("Testowy");
        registerDTO.setRoleType(RoleType.STUDENT);
        
        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = registerResult.getResponse().getContentAsString();
        MessageResponse messageResponse = objectMapper.readValue(responseContent, MessageResponse.class);
        assertThat(messageResponse.getMessage()).isEqualTo("User registered successfully!");

        User newUser = userRepository.findByEmail(newStudentEmail).orElse(null);
        assertThat(newUser).isNotNull();
        assertThat(newUser.getFirstName()).isEqualTo("Jan");
        assertThat(newUser.getLastName()).isEqualTo("Testowy");
        assertThat(newUser.getRole().getRoleName()).isEqualTo(RoleType.STUDENT);
        assertThat(newUser.getIsActive()).isTrue();

        assertThat(newUser.getStudentProfile()).isNotNull();
        assertThat(newUser.getStudentProfile().getIndexNumber()).isNotBlank();
        assertThat(studentRepository.existsByIndexNumber(newUser.getStudentProfile().getIndexNumber())).isTrue();
        assertThat(newUser.getStudentProfile().getUser().getUserId()).isEqualTo(newUser.getUserId());
    }

    @Test
    void studentShouldNotBeAbleToRegisterNewUser() throws Exception {
        LoginDTO studentLoginDTO = new LoginDTO();
        studentLoginDTO.setEmail("student@university.com");
        studentLoginDTO.setPassword("student123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentLoginDTO)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie studentJwtCookie = loginResult.getResponse().getCookie(JWT_COOKIE_NAME);
        assertThat(studentJwtCookie).isNotNull();

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("another.student@example.com");
        registerDTO.setPassword("password123");
        registerDTO.setFirstName("Anna");
        registerDTO.setLastName("Kolejna");
        registerDTO.setRoleType(RoleType.STUDENT);

        mockMvc.perform(post("/api/users/register")
                        .cookie(studentJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isForbidden());
    }
}