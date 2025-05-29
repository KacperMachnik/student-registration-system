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
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.LoginDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseGroupRepository courseGroupRepository;
    @Autowired
    private StudentRepository studentRepository;

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

    private CourseResponse testCourse;
    private Teacher testTeacher;
    private Student testStudent;


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

    private Teacher createAndSaveTestTeacher(String email, String firstName, String lastName, String title, RoleType roleType) {
         User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword(userRepository.findByEmail(ADMIN_EMAIL).get().getPassword());
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole(roleRepository.findByRoleName(roleType).orElseThrow());
            newUser.setIsActive(true);
            return userRepository.save(newUser);
        });

        return teacherRepository.findByUser(user).orElseGet(() -> {
            Teacher newTeacher = new Teacher();
            newTeacher.setUser(user);
            newTeacher.setTitle(title);
            return teacherRepository.save(newTeacher);
        });
    }
    @Autowired
    private RoleRepository roleRepository;


    @BeforeEach
    void setUp() throws Exception {
        adminCookie = loginAndGetCookie(ADMIN_EMAIL, ADMIN_PASSWORD, RoleType.DEANERY_STAFF);
        studentCookie = loginAndGetCookie(STUDENT_EMAIL, STUDENT_PASSWORD, RoleType.STUDENT);
        teacherCookie = loginAndGetCookie(TEACHER_EMAIL, TEACHER_PASSWORD, RoleType.TEACHER);

        testCourse = createTestCourse("GRPTEST-C1", "Group Test Course", 3, adminCookie);
        testTeacher = teacherRepository.findByUser(userRepository.findByEmail(TEACHER_EMAIL).get()).orElseThrow();
        testStudent = studentRepository.findByUser(userRepository.findByEmail(STUDENT_EMAIL).get()).orElseThrow();

    }

    @Test
    void deaneryStaffShouldCreateGroupSuccessfully() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(testCourse.getCourseId());
        createGroupDTO.setTeacherId(testTeacher.getTeacherId());
        createGroupDTO.setGroupNumber(101);
        createGroupDTO.setMaxCapacity(20);

        mockMvc.perform(post("/api/groups")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").exists())
                .andExpect(jsonPath("$.groupNumber").value(101))
                .andExpect(jsonPath("$.maxCapacity").value(20))
                .andExpect(jsonPath("$.course.courseId").value(testCourse.getCourseId()))
                .andExpect(jsonPath("$.teacher.teacherId").value(testTeacher.getTeacherId()));

        assertThat(courseGroupRepository.findAll().stream()
                .anyMatch(g -> g.getGroupNumber() == 101 && g.getCourse().getCourseId().equals(testCourse.getCourseId())))
                .isTrue();
    }

    @Test
    void createGroupForNonExistentCourseShouldFail() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(9999L);
        createGroupDTO.setTeacherId(testTeacher.getTeacherId());
        createGroupDTO.setGroupNumber(102);
        createGroupDTO.setMaxCapacity(15);

        mockMvc.perform(post("/api/groups")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGroupWithNonExistentTeacherShouldFail() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(testCourse.getCourseId());
        createGroupDTO.setTeacherId(8888L);
        createGroupDTO.setGroupNumber(103);
        createGroupDTO.setMaxCapacity(10);

        mockMvc.perform(post("/api/groups")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentShouldNotCreateGroup() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(testCourse.getCourseId());
        createGroupDTO.setTeacherId(testTeacher.getTeacherId());
        createGroupDTO.setGroupNumber(104);
        createGroupDTO.setMaxCapacity(5);

        mockMvc.perform(post("/api/groups")
                        .cookie(studentCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isForbidden());
    }

    private GroupResponse createAndEnrollStudentInGroup(CourseResponse course, Teacher teacher, Student student, Cookie adminCookieForGroupCreation, Cookie studentCookieForEnroll) throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(course.getCourseId());
        createGroupDTO.setTeacherId(teacher.getTeacherId());
        createGroupDTO.setGroupNumber(201);
        createGroupDTO.setMaxCapacity(1);

        MvcResult groupResult = mockMvc.perform(post("/api/groups")
                        .cookie(adminCookieForGroupCreation)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        GroupResponse createdGroup = objectMapper.readValue(groupResult.getResponse().getContentAsString(), GroupResponse.class);

        mockMvc.perform(post("/api/enrollments/my/" + createdGroup.getGroupId())
                        .cookie(studentCookieForEnroll))
                .andExpect(status().isCreated());
        return createdGroup;
    }


    @Test
    void shouldGetGroupDetailsByIdForAuthorizedUsers() throws Exception {
        GroupResponse group = createAndEnrollStudentInGroup(testCourse, testTeacher, testStudent, adminCookie, studentCookie);
        Long groupId = group.getGroupId();

        mockMvc.perform(get("/api/groups/" + groupId)
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId))
                .andExpect(jsonPath("$.groupNumber").value(201));

        mockMvc.perform(get("/api/groups/" + groupId)
                        .cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId));

        mockMvc.perform(get("/api/groups/" + groupId)
                        .cookie(teacherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId));
    }

    @Test
    void studentNotEnrolledShouldNotGetGroupDetails() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(testCourse.getCourseId());
        createGroupDTO.setTeacherId(testTeacher.getTeacherId());
        createGroupDTO.setGroupNumber(202);
        createGroupDTO.setMaxCapacity(5);

        MvcResult groupResult = mockMvc.perform(post("/api/groups")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        GroupResponse createdGroup = objectMapper.readValue(groupResult.getResponse().getContentAsString(), GroupResponse.class);

        mockMvc.perform(get("/api/groups/" + createdGroup.getGroupId())
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAvailableGroupsForStudent() throws Exception {
        CourseResponse course1 = createTestCourse("AVAIL-C1", "Available Course 1", 3, adminCookie);
        CourseResponse course2 = createTestCourse("AVAIL-C2", "Course Student Enrolled", 3, adminCookie);
        Teacher teacher1 = createAndSaveTestTeacher("availt1@example.com", "Avail", "Teach1", "Dr", RoleType.TEACHER);

        CreateGroupDTO groupDto1 = new CreateGroupDTO();
        groupDto1.setCourseId(course1.getCourseId());
        groupDto1.setTeacherId(teacher1.getTeacherId());
        groupDto1.setGroupNumber(301);
        groupDto1.setMaxCapacity(10);
        mockMvc.perform(post("/api/groups").cookie(adminCookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupDto1))).andExpect(status().isCreated());

        GroupResponse enrolledGroup = createAndEnrollStudentInGroup(course2, teacher1, testStudent, adminCookie, studentCookie);

        mockMvc.perform(get("/api/groups/available")
                        .cookie(studentCookie)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.course.courseCode == 'AVAIL-C1' && @.groupNumber == 301)]").exists())
                .andExpect(jsonPath("$.content[?(@.groupId == " + enrolledGroup.getGroupId() + ")]").doesNotExist());
    }

    @Test
    void shouldGetStudentsInGroupForDeaneryAndTeacher() throws Exception {
        GroupResponse group = createAndEnrollStudentInGroup(testCourse, testTeacher, testStudent, adminCookie, studentCookie);
        Long groupId = group.getGroupId();

        mockMvc.perform(get("/api/groups/" + groupId + "/students")
                        .cookie(adminCookie)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].studentId").value(testStudent.getStudentId()));

        mockMvc.perform(get("/api/groups/" + groupId + "/students")
                        .cookie(teacherCookie)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].studentId").value(testStudent.getStudentId()));
    }

    @Test
    void studentShouldNotGetStudentsInGroup() throws Exception {
        GroupResponse group = createAndEnrollStudentInGroup(testCourse, testTeacher, testStudent, adminCookie, studentCookie);
        Long groupId = group.getGroupId();

        mockMvc.perform(get("/api/groups/" + groupId + "/students")
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());
    }

}