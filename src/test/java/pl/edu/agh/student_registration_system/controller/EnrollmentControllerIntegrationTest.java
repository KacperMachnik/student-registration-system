package pl.edu.agh.student_registration_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.repository.*;
import pl.edu.agh.student_registration_system.service.UserService;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EnrollmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserService userService;

    private final String ADMIN_EMAIL = "admin@university.com";
    private final String ADMIN_PASSWORD = "admin123";
    private final String TEACHER_EMAIL = "teacher@university.com";
    private final String STUDENT_EMAIL = "student@university.com";
    private final String STUDENT_PASSWORD = "student123";
    private final String JWT_COOKIE_NAME = "student-registration";

    private Cookie adminCookie;
    private Cookie studentCookie;
    private Cookie student2Cookie;

    private CourseResponse testCourse1;
    private CourseResponse testCourse2;
    private Teacher testTeacher;
    private Student defaultStudentEntity;
    private Student student2Entity;

    private Cookie loginAndGetCookie(String email, String password, RoleType expectedRole) throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk()).andReturn();
        String loginResponseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseContent, LoginResponse.class);
        assertThat(loginResponse.getUsername()).isEqualTo(email);
        assertThat(loginResponse.getRoles()).contains(expectedRole.name());
        Cookie jwtCookie = loginResult.getResponse().getCookie(JWT_COOKIE_NAME);
        assertThat(jwtCookie).isNotNull();
        return jwtCookie;
    }

    private CourseResponse createTestCourse(String code, String name, int credits, Cookie cookie) throws Exception {
        CreateCourseDTO createDto = new CreateCourseDTO();
        createDto.setCourseCode(code);
        createDto.setCourseName(name);
        createDto.setCredits(credits);
        MvcResult result = mockMvc.perform(post("/api/courses").cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), CourseResponse.class);
    }

    private GroupResponse createTestGroup(Long courseId, Long teacherId, int groupNumber, int capacity, Cookie cookie) throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(courseId);
        createGroupDTO.setTeacherId(teacherId);
        createGroupDTO.setGroupNumber(groupNumber);
        createGroupDTO.setMaxCapacity(capacity);
        MvcResult result = mockMvc.perform(post("/api/groups").cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), GroupResponse.class);
    }

    private User createAndSaveNewStudentUser(String email, String firstName, String lastName, String password) {
        if (userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).get();
        }
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(email);
        registerDTO.setFirstName(firstName);
        registerDTO.setLastName(lastName);
        registerDTO.setPassword(password);
        registerDTO.setRoleType(RoleType.STUDENT);
        return userService.registerNewUser(registerDTO);
    }


    @BeforeEach
     void setUp() throws Exception {
        adminCookie = loginAndGetCookie(ADMIN_EMAIL, ADMIN_PASSWORD, RoleType.DEANERY_STAFF);
        studentCookie = loginAndGetCookie(STUDENT_EMAIL, STUDENT_PASSWORD, RoleType.STUDENT);

        User defaultUserForStudent = userRepository.findByEmail(STUDENT_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", STUDENT_EMAIL));
        defaultStudentEntity = studentRepository.findByUser(defaultUserForStudent)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile for user", "email", STUDENT_EMAIL));

        User student2User = createAndSaveNewStudentUser("student2.test@example.com", "Jan", "Drugi", "student2pass");
        student2Cookie = loginAndGetCookie("student2.test@example.com", "student2pass", RoleType.STUDENT);
        student2Entity = studentRepository.findByUser(student2User)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile for user", "email", "student2.test@example.com"));

        User teacherUserEntity = userRepository.findByEmail(TEACHER_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", TEACHER_EMAIL));
        
        testTeacher = teacherRepository.findByUser(teacherUserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile for user", "email", TEACHER_EMAIL));


        testCourse1 = createTestCourse("ENRL-C1", "Enrollment Course 1", 3, adminCookie);
        testCourse2 = createTestCourse("ENRL-C2", "Enrollment Course 2", 4, adminCookie);
    }

    @Test
    void studentShouldEnrollInAvailableGroupSuccessfully() throws Exception {
        GroupResponse group = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 301, 5, adminCookie);

        mockMvc.perform(post("/api/enrollments/my/" + group.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enrollmentId").exists())
                .andExpect(jsonPath("$.studentId").value(defaultStudentEntity.getStudentId()))
                .andExpect(jsonPath("$.groupId").value(group.getGroupId()));

        assertThat(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(defaultStudentEntity, group.getGroupId())).isTrue();
    }

    @Test
    void studentShouldNotEnrollInFullGroup() throws Exception {
        GroupResponse group = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 302, 1, adminCookie);

        mockMvc.perform(post("/api/enrollments/my/" + group.getGroupId())
                        .cookie(student2Cookie))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/enrollments/my/" + group.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot enroll: Group capacity is full (1/1)."));
    }

    @Test
    void studentShouldNotEnrollIfAlreadyEnrolledInSameCourse() throws Exception {
        GroupResponse group1 = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 303, 5, adminCookie);
        mockMvc.perform(post("/api/enrollments/my/" + group1.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isCreated());

        GroupResponse group2 = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 304, 5, adminCookie);
        mockMvc.perform(post("/api/enrollments/my/" + group2.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Student is already enrolled in a group for course: " + testCourse1.getCourseCode()));
    }

    @Test
    void studentShouldUnenrollFromGroupSuccessfully() throws Exception {
        GroupResponse group = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 305, 5, adminCookie);
        mockMvc.perform(post("/api/enrollments/my/" + group.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isCreated());
        assertThat(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(defaultStudentEntity, group.getGroupId())).isTrue();

        mockMvc.perform(delete("/api/enrollments/my/" + group.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isNoContent());

        assertThat(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(defaultStudentEntity, group.getGroupId())).isFalse();
    }

    @Test
    void studentUnenrollFromGroupNotEnrolledShouldFail() throws Exception {
        GroupResponse group = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 306, 5, adminCookie);

        mockMvc.perform(delete("/api/enrollments/my/" + group.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminShouldEnrollStudentToGroup() throws Exception {
        GroupResponse group = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 307, 1, adminCookie);

        mockMvc.perform(post("/api/enrollments/admin/groups/" + group.getGroupId() + "/students/" + student2Entity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(student2Entity.getStudentId()))
                .andExpect(jsonPath("$.groupId").value(group.getGroupId()));

        assertThat(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student2Entity, group.getGroupId())).isTrue();
    }

    @Test
    void adminShouldUnenrollStudentFromGroup() throws Exception {
        GroupResponse group = createTestGroup(testCourse1.getCourseId(), testTeacher.getTeacherId(), 308, 5, adminCookie);
        mockMvc.perform(post("/api/enrollments/admin/groups/" + group.getGroupId() + "/students/" + student2Entity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isCreated());
        assertThat(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student2Entity, group.getGroupId())).isTrue();

        mockMvc.perform(delete("/api/enrollments/admin/groups/" + group.getGroupId() + "/students/" + student2Entity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isNoContent());

        assertThat(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student2Entity, group.getGroupId())).isFalse();
    }

     @Test
    void adminEnrollStudentAlreadyEnrolledInCourseShouldFail() throws Exception {
        GroupResponse group1 = createTestGroup(testCourse2.getCourseId(), testTeacher.getTeacherId(), 401, 5, adminCookie);
        mockMvc.perform(post("/api/enrollments/my/" + group1.getGroupId())
                        .cookie(student2Cookie))
                .andExpect(status().isCreated());

        GroupResponse group2 = createTestGroup(testCourse2.getCourseId(), testTeacher.getTeacherId(), 402, 5, adminCookie);
        mockMvc.perform(post("/api/enrollments/admin/groups/" + group2.getGroupId() + "/students/" + student2Entity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Student is already enrolled in a group for course: " + testCourse2.getCourseCode()));
    }
}