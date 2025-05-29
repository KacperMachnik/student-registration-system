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
import pl.edu.agh.student_registration_system.payload.dto.*;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.*;
import pl.edu.agh.student_registration_system.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GradeControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private GradeRepository gradeRepository;


    private final String ADMIN_EMAIL = "admin@university.com";
    private final String ADMIN_PASSWORD = "admin123";
    private final String TEACHER_EMAIL = "teacher@university.com";
    private final String TEACHER_PASSWORD = "teacher123";
    private final String STUDENT_EMAIL = "student@university.com";
    private final String STUDENT_PASSWORD = "student123";
    private final String JWT_COOKIE_NAME = "student-registration";

    private Cookie adminCookie;
    private Cookie teacher1Cookie;
    private Cookie teacher2Cookie;
    private Cookie studentCookie;

    private CourseResponse testCourse;
    private Teacher teacher1Entity;
    private Teacher teacher2Entity;
    private Student studentEntity;
    private GroupResponse testGroup;

    private Cookie loginAndGetCookie(String email, String password, RoleType expectedRole) throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk()).andReturn();
        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
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
     private User createAndSaveNewUserWithRole(String email, String firstName, String lastName, String password, RoleType roleType, String titleIfTeacher) {
        if (userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).get();
        }
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(email);
        registerDTO.setFirstName(firstName);
        registerDTO.setLastName(lastName);
        registerDTO.setPassword(password);
        registerDTO.setRoleType(roleType);
        if (roleType == RoleType.TEACHER) {
            registerDTO.setTitle(titleIfTeacher);
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(roleRepository.findByRoleName(roleType).orElseThrow());
        user.setIsActive(true);

        if (roleType == RoleType.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setTitle(titleIfTeacher);
            teacher.setUser(user);
            user.setTeacherProfile(teacher);
        } else if (roleType == RoleType.STUDENT) {
             Student student = new Student();
             String indexNumber;
             do {
                 indexNumber = String.valueOf(100000 + (int)(Math.random() * 900000));
             } while (studentRepository.existsByIndexNumber(indexNumber));
             student.setIndexNumber(indexNumber);
             student.setUser(user);
             user.setStudentProfile(student);
        }
        return userRepository.save(user);
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        adminCookie = loginAndGetCookie(ADMIN_EMAIL, ADMIN_PASSWORD, RoleType.DEANERY_STAFF);
        teacher1Cookie = loginAndGetCookie(TEACHER_EMAIL, TEACHER_PASSWORD, RoleType.TEACHER);

        User teacher2User = createAndSaveNewUserWithRole("teacher2.test@example.com", "Maria", "Inna", "teacher2pass", RoleType.TEACHER, "Mgr");
        teacher2Cookie = loginAndGetCookie("teacher2.test@example.com", "teacher2pass", RoleType.TEACHER);
        teacher2Entity = teacherRepository.findByUser(teacher2User)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile for user", "email", "teacher2.test@example.com"));


        studentCookie = loginAndGetCookie(STUDENT_EMAIL, STUDENT_PASSWORD, RoleType.STUDENT);
        User studentUserEntity = userRepository.findByEmail(STUDENT_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", STUDENT_EMAIL));
        studentEntity = studentRepository.findByUser(studentUserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile for user", "email", STUDENT_EMAIL));

        User teacher1UserEntity = userRepository.findByEmail(TEACHER_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", TEACHER_EMAIL));
        teacher1Entity = teacherRepository.findByUser(teacher1UserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile for user", "email", TEACHER_EMAIL));


        testCourse = createTestCourse("GRADE-C1", "Grading Course", 3, adminCookie);
        testGroup = createTestGroup(testCourse.getCourseId(), teacher1Entity.getTeacherId(), 501, 10, adminCookie);

        mockMvc.perform(post("/api/enrollments/admin/groups/" + testGroup.getGroupId() + "/students/" + studentEntity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isCreated());
    }

    private GradeResponse addGradeViaApi(Long studentId, Long courseId, String value, String comment, Cookie performerCookie) throws Exception {
        CreateGradeDTO createGradeDTO = new CreateGradeDTO();
        createGradeDTO.setStudentId(studentId);
        createGradeDTO.setCourseId(courseId);
        createGradeDTO.setGradeValue(value);
        createGradeDTO.setComment(comment);

        MvcResult result = mockMvc.perform(post("/api/grades")
                        .cookie(performerCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGradeDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), GradeResponse.class);
    }

    @Test
    void teacherShouldAddGradeForTheirStudentInCourse() throws Exception {
        GradeResponse grade = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "5.0", "Bardzo dobrze", teacher1Cookie);

        assertThat(grade.getGradeId()).isNotNull();
        assertThat(grade.getStudent().getStudentId()).isEqualTo(studentEntity.getStudentId());
        assertThat(grade.getCourse().getCourseId()).isEqualTo(testCourse.getCourseId());
        assertThat(grade.getTeacher().getTeacherId()).isEqualTo(teacher1Entity.getTeacherId());
        assertThat(grade.getGradeValue()).isEqualTo("5.0");

        assertThat(gradeRepository.existsById(grade.getGradeId())).isTrue();
    }

    @Test
    void deaneryStaffShouldNotAddGrade() throws Exception {
        CreateGradeDTO createGradeDTO = new CreateGradeDTO();
        createGradeDTO.setStudentId(studentEntity.getStudentId());
        createGradeDTO.setCourseId(testCourse.getCourseId());
        createGradeDTO.setGradeValue("4.0");

        mockMvc.perform(post("/api/grades")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGradeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void issuingTeacherShouldUpdateGrade() throws Exception {
        GradeResponse originalGrade = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "3.5", "Do poprawy", teacher1Cookie);

        UpdateGradeDTO updateDTO = new UpdateGradeDTO();
        updateDTO.setGradeValue("4.0");
        updateDTO.setComment("Poprawione");

        mockMvc.perform(put("/api/grades/" + originalGrade.getGradeId())
                        .cookie(teacher1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeValue").value("4.0"))
                .andExpect(jsonPath("$.comment").value("Poprawione"));
    }

    @Test
    void deaneryStaffShouldUpdateGrade() throws Exception {
        GradeResponse originalGrade = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "2.0", "Słabo", teacher1Cookie);

        UpdateGradeDTO updateDTO = new UpdateGradeDTO();
        updateDTO.setGradeValue("3.0");
        updateDTO.setComment("Admin poprawił");

        mockMvc.perform(put("/api/grades/" + originalGrade.getGradeId())
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeValue").value("3.0"));
    }

    @Test
    void otherTeacherShouldNotUpdateGrade() throws Exception {
        GradeResponse originalGrade = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "4.5", "Super", teacher1Cookie);

        UpdateGradeDTO updateDTO = new UpdateGradeDTO();
        updateDTO.setGradeValue("1.0");

        mockMvc.perform(put("/api/grades/" + originalGrade.getGradeId())
                        .cookie(teacher2Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void issuingTeacherShouldDeleteGrade() throws Exception {
        GradeResponse gradeToDelete = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "5.0", "Na usunięcie", teacher1Cookie);
        assertThat(gradeRepository.existsById(gradeToDelete.getGradeId())).isTrue();

        mockMvc.perform(delete("/api/grades/" + gradeToDelete.getGradeId())
                        .cookie(teacher1Cookie))
                .andExpect(status().isNoContent());

        assertThat(gradeRepository.existsById(gradeToDelete.getGradeId())).isFalse();
    }

    @Test
    void deaneryStaffShouldDeleteGrade() throws Exception {
        GradeResponse gradeToDelete = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "2.5", "Admin usuwa", teacher1Cookie);
        assertThat(gradeRepository.existsById(gradeToDelete.getGradeId())).isTrue();

        mockMvc.perform(delete("/api/grades/" + gradeToDelete.getGradeId())
                        .cookie(adminCookie))
                .andExpect(status().isNoContent());

        assertThat(gradeRepository.existsById(gradeToDelete.getGradeId())).isFalse();
    }

    @Test
    void otherTeacherShouldNotDeleteGrade() throws Exception {
        GradeResponse gradeToDelete = addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "3.0", "Nie usuniesz", teacher1Cookie);

        mockMvc.perform(delete("/api/grades/" + gradeToDelete.getGradeId())
                        .cookie(teacher2Cookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizedUserShouldGetStudentGradesForCourse() throws Exception {
        addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "4.0", "Komentarz 1", teacher1Cookie);
        addGradeViaApi(studentEntity.getStudentId(), testCourse.getCourseId(), "4.5", "Komentarz 2", teacher1Cookie);

        mockMvc.perform(get("/api/grades/student/" + studentEntity.getStudentId() + "/course/" + testCourse.getCourseId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].student.studentId").value(studentEntity.getStudentId()))
                .andExpect(jsonPath("$[0].course.courseId").value(testCourse.getCourseId()));

        mockMvc.perform(get("/api/grades/student/" + studentEntity.getStudentId() + "/course/" + testCourse.getCourseId())
                        .cookie(teacher1Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/grades/student/" + studentEntity.getStudentId() + "/course/" + testCourse.getCourseId())
                        .cookie(teacher2Cookie))
                .andExpect(status().isForbidden());
    }
}