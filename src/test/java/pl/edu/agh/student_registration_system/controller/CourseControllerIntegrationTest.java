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
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateCourseDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.LoginResponse;
import pl.edu.agh.student_registration_system.repository.CourseRepository;
import pl.edu.agh.student_registration_system.repository.TeacherRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import pl.edu.agh.student_registration_system.model.User;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    private final String ADMIN_EMAIL = "admin@university.com";
    private final String ADMIN_PASSWORD = "admin123";
    private final String STUDENT_EMAIL = "student@university.com";
    private final String STUDENT_PASSWORD = "student123";
    private final String TEACHER_EMAIL = "teacher@university.com";
    private final String TEACHER_PASSWORD = "teacher123";
    private final String JWT_COOKIE_NAME = "student-registration";

    private Cookie adminCookie;
    private Cookie studentCookie;
    private Cookie teacherCookie;

    private Cookie loginAndGetCookie(String email, String password, RoleType expectedRole) throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseContent = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(loginResponseContent, LoginResponse.class);
        assertThat(loginResponse.getUsername()).isEqualTo(email);
        assertThat(loginResponse.getRoles()).contains(expectedRole.name());

        Cookie jwtCookie = loginResult.getResponse().getCookie(JWT_COOKIE_NAME);
        assertThat(jwtCookie).isNotNull();
        return jwtCookie;
    }

    @BeforeEach
    void setUp() throws Exception {
        adminCookie = loginAndGetCookie(ADMIN_EMAIL, ADMIN_PASSWORD, RoleType.DEANERY_STAFF);
        studentCookie = loginAndGetCookie(STUDENT_EMAIL, STUDENT_PASSWORD, RoleType.STUDENT);
        teacherCookie = loginAndGetCookie(TEACHER_EMAIL, TEACHER_PASSWORD, RoleType.TEACHER);
    }

    @Test
    void deaneryStaffShouldCreateCourseSuccessfully() throws Exception {
        CreateCourseDTO createCourseDTO = new CreateCourseDTO();
        createCourseDTO.setCourseCode("IT-CS101");
        createCourseDTO.setCourseName("Introduction to Computer Science");
        createCourseDTO.setDescription("Fundamental concepts of CS.");
        createCourseDTO.setCredits(5);

        mockMvc.perform(post("/api/courses")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCourseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").exists())
                .andExpect(jsonPath("$.courseCode").value("IT-CS101"))
                .andExpect(jsonPath("$.courseName").value("Introduction to Computer Science"))
                .andExpect(jsonPath("$.credits").value(5));

        Course savedCourse = courseRepository.findAll().stream()
                .filter(c -> "IT-CS101".equals(c.getCourseCode()))
                .findFirst().orElse(null);
        assertThat(savedCourse).isNotNull();
        assertThat(savedCourse.getCourseName()).isEqualTo("Introduction to Computer Science");
    }

    @Test
    void studentShouldNotCreateCourse() throws Exception {
        CreateCourseDTO createCourseDTO = new CreateCourseDTO();
        createCourseDTO.setCourseCode("ST-FL202");
        createCourseDTO.setCourseName("Student Failed Course");

        mockMvc.perform(post("/api/courses")
                        .cookie(studentCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCourseDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatingCourseWithDuplicateCodeShouldFail() throws Exception {
        CreateCourseDTO firstCourseDTO = new CreateCourseDTO();
        String duplicateCode = "DUP-CODE1";
        firstCourseDTO.setCourseCode(duplicateCode);
        firstCourseDTO.setCourseName("First Course with DUP-CODE1");
        firstCourseDTO.setCredits(3);

        mockMvc.perform(post("/api/courses")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstCourseDTO)))
                .andExpect(status().isCreated());

        CreateCourseDTO secondCourseDTO = new CreateCourseDTO();
        secondCourseDTO.setCourseCode(duplicateCode);
        secondCourseDTO.setCourseName("Second Course with DUP-CODE1");
        secondCourseDTO.setCredits(4);

        mockMvc.perform(post("/api/courses")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondCourseDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetCourseByIdForAuthenticatedUsers() throws Exception {
        CreateCourseDTO createDto = new CreateCourseDTO();
        String courseCode = "GET-C404";
        createDto.setCourseCode(courseCode);
        createDto.setCourseName("Course To Get");
        createDto.setCredits(4);

        MvcResult createResult = mockMvc.perform(post("/api/courses")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();
        CourseResponse createdCourse = objectMapper.readValue(createResult.getResponse().getContentAsString(), CourseResponse.class);
        Long courseId = createdCourse.getCourseId();

        mockMvc.perform(get("/api/courses/" + courseId)
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.courseCode").value(courseCode));

        mockMvc.perform(get("/api/courses/" + courseId)
                        .cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.courseCode").value(courseCode));

        mockMvc.perform(get("/api/courses/" + courseId)
                        .cookie(teacherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.courseCode").value(courseCode));
    }

    @Test
    void getNonExistentCourseShouldReturnNotFound() throws Exception {
        Long nonExistentCourseId = 99999L;
        mockMvc.perform(get("/api/courses/" + nonExistentCourseId)
                        .cookie(adminCookie))
                .andExpect(status().isNotFound());
    }

    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private UserRepository userRepository;


    private CourseResponse createTestCourse(String code, String name, int credits, Cookie cookie) throws Exception {
        CreateCourseDTO createDto = new CreateCourseDTO();
        createDto.setCourseCode(code);
        createDto.setCourseName(name);
        createDto.setCredits(credits);

        MvcResult result = mockMvc.perform(post("/api/courses")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), CourseResponse.class);
    }

    private GroupResponse createTestGroup(Long courseId, Long teacherId, int groupNumber, int capacity, Cookie cookie) throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(courseId);
        createGroupDTO.setTeacherId(teacherId);
        createGroupDTO.setGroupNumber(groupNumber);
        createGroupDTO.setMaxCapacity(capacity);

        MvcResult result = mockMvc.perform(post("/api/groups")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), GroupResponse.class);
    }

    private Teacher createAndSaveTestTeacher(String email, String firstName, String lastName, String title) {
        if (userRepository.existsByEmail(email)) {
            User existingUser = userRepository.findByEmail(email).get();
            return teacherRepository.findByUser(existingUser).orElseGet(() -> {
                Teacher newTeacher = new Teacher();
                newTeacher.setUser(existingUser);
                newTeacher.setTitle(title);
                return teacherRepository.save(newTeacher);
            });
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword("testPassword123");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(userRepository.findByEmail(ADMIN_EMAIL).get().getRole());
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(savedUser);
        teacher.setTitle(title);
        return teacherRepository.save(teacher);
    }


    @Test
    void deaneryStaffShouldUpdateCourseSuccessfully() throws Exception {
        CourseResponse existingCourse = createTestCourse("UPD-C001", "Course To Update", 3, adminCookie);
        Long courseId = existingCourse.getCourseId();

        UpdateCourseDTO updateDTO = new UpdateCourseDTO();
        updateDTO.setCourseName("Updated Course Name");
        updateDTO.setCredits(4);
        
        mockMvc.perform(patch("/api/courses/" + courseId)
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.courseName").value("Updated Course Name"))
                .andExpect(jsonPath("$.courseCode").value("UPD-C001"))
                .andExpect(jsonPath("$.credits").value(4));

        Course updatedCourseFromDb = courseRepository.findById(courseId).orElse(null);
        assertThat(updatedCourseFromDb).isNotNull();
        assertThat(updatedCourseFromDb.getCourseName()).isEqualTo("Updated Course Name");
        assertThat(updatedCourseFromDb.getCredits()).isEqualTo(4);
    }

    @Test
    void studentShouldNotUpdateCourse() throws Exception {
        CourseResponse existingCourse = createTestCourse("UPD-S002", "Student Cant Update", 3, adminCookie);
        Long courseId = existingCourse.getCourseId();

        UpdateCourseDTO updateDTO = new UpdateCourseDTO();
        updateDTO.setCourseName("Attempted Update By Student");

        mockMvc.perform(patch("/api/courses/" + courseId)
                        .cookie(studentCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deaneryStaffShouldDeleteCourseSuccessfully() throws Exception {
        CourseResponse courseToDelete = createTestCourse("DEL-C003", "Course To Delete", 2, adminCookie);
        Long courseId = courseToDelete.getCourseId();

        assertThat(courseRepository.existsById(courseId)).isTrue();

        mockMvc.perform(delete("/api/courses/" + courseId)
                        .cookie(adminCookie))
                .andExpect(status().isNoContent());

        assertThat(courseRepository.existsById(courseId)).isFalse();
    }

    @Test
    void teacherShouldNotDeleteCourse() throws Exception {
        CourseResponse courseToDelete = createTestCourse("DEL-T004", "Teacher Cant Delete", 2, adminCookie);
        Long courseId = courseToDelete.getCourseId();

        mockMvc.perform(delete("/api/courses/" + courseId)
                        .cookie(teacherCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSearchCoursesWithoutFilters() throws Exception {
        createTestCourse("SEARCH01", "Alpha Course", 3, adminCookie);
        createTestCourse("SEARCH02", "Beta Course", 4, adminCookie);
        createTestCourse("SEARCH03", "Gamma Course", 5, adminCookie);

        mockMvc.perform(get("/api/courses")
                        .cookie(adminCookie)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.content[?(@.courseCode == 'SEARCH01')]").exists())
                .andExpect(jsonPath("$.content[?(@.courseCode == 'SEARCH02')]").exists());
    }

//     @Test
//     void shouldGetGroupsForCourse() throws Exception {
//         CourseResponse course = createTestCourse("GRP-C100", "Course With Groups", 3, adminCookie);
//         Teacher teacher = createAndSaveTestTeacher("grpteach@example.com", "Group", "Teacher", "Dr");

//         createTestGroup(course.getCourseId(), teacher.getTeacherId(), 1, 20, adminCookie);
//         createTestGroup(course.getCourseId(), teacher.getTeacherId(), 2, 25, adminCookie);

//         mockMvc.perform(get("/api/courses/" + course.getCourseId() + "/groups")
//                         .cookie(adminCookie))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$").isArray())
//                 .andExpect(jsonPath("$", hasSize(2)))
//                 .andExpect(jsonPath("$[0].groupNumber").value(1))
//                 .andExpect(jsonPath("$[1].groupNumber").value(2))
//                 .andExpect(jsonPath("$[0].course.courseId").value(course.getCourseId()))
//                 .andExpect(jsonPath("$[0].teacher.teacherId").value(teacher.getTeacherId()));
//     }

    @Test
    void getGroupsForNonExistentCourseShouldReturnNotFound() throws Exception {
        Long nonExistentCourseId = 88888L;
        mockMvc.perform(get("/api/courses/" + nonExistentCourseId + "/groups")
                        .cookie(adminCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void deaneryStaffShouldDeleteAllGroupsForCourse() throws Exception {
        CourseResponse course = createTestCourse("DELGRP-C200", "Course For Group Deletion", 3, adminCookie);
        Teacher teacher = createAndSaveTestTeacher("delgrpteach@example.com", "DelGroup", "Teacher", "Prof");

        GroupResponse group1 = createTestGroup(course.getCourseId(), teacher.getTeacherId(), 10, 15, adminCookie);
        GroupResponse group2 = createTestGroup(course.getCourseId(), teacher.getTeacherId(), 20, 15, adminCookie);

        MvcResult getResult = mockMvc.perform(get("/api/courses/" + course.getCourseId() + "/groups")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();
        List<GroupResponse> groupsBeforeDelete = objectMapper.readValue(getResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, GroupResponse.class));
        assertThat(groupsBeforeDelete.stream().anyMatch(g -> g.getGroupId().equals(group1.getGroupId()))).isTrue();

        mockMvc.perform(delete("/api/courses/" + course.getCourseId() + "/groups")
                        .cookie(adminCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/courses/" + course.getCourseId() + "/groups")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void studentShouldNotDeleteAllGroupsForCourse() throws Exception {
        CourseResponse course = createTestCourse("DELGRP-S300", "Student Cant Delete Groups", 3, adminCookie);

        mockMvc.perform(delete("/api/courses/" + course.getCourseId() + "/groups")
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());
    }
}