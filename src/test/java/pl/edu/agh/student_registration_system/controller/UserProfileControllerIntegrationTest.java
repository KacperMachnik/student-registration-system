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


import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.empty;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserProfileControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;


    private final String ADMIN_EMAIL = "admin@university.com";
    private final String ADMIN_PASSWORD = "admin123";
    private final String TEACHER_EMAIL = "teacher@university.com";
    private final String TEACHER_PASSWORD = "teacher123";
    private final String STUDENT_EMAIL = "student@university.com";
    private final String STUDENT_PASSWORD = "student123";
    private final String JWT_COOKIE_NAME = "student-registration";

    private Cookie adminCookie;
    private Cookie teacherCookie;
    private Cookie studentCookie;

    private Student studentEntity;
    private Teacher teacherEntity;
    private CourseResponse course1, course2;
    private GroupResponse group1Course1Teacher, group2Course1Teacher, group1Course2Teacher;
    private MeetingResponse meeting1Group1;

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

    @BeforeEach
    void setUp() throws Exception {
        adminCookie = loginAndGetCookie(ADMIN_EMAIL, ADMIN_PASSWORD, RoleType.DEANERY_STAFF);
        teacherCookie = loginAndGetCookie(TEACHER_EMAIL, TEACHER_PASSWORD, RoleType.TEACHER);
        studentCookie = loginAndGetCookie(STUDENT_EMAIL, STUDENT_PASSWORD, RoleType.STUDENT);

        User studentUserEntity = userRepository.findByEmail(STUDENT_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", STUDENT_EMAIL));
        studentEntity = studentRepository.findByUser(studentUserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile for user", "email", STUDENT_EMAIL));

        User teacherUserEntity = userRepository.findByEmail(TEACHER_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", TEACHER_EMAIL));
        teacherEntity = teacherRepository.findByUser(teacherUserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile for user", "email", TEACHER_EMAIL));

        course1 = createTestCourse("PROF-C1", "Profile Course 1", 3, adminCookie);
        course2 = createTestCourse("PROF-C2", "Profile Course 2", 4, adminCookie);

        group1Course1Teacher = createTestGroup(course1.getCourseId(), teacherEntity.getTeacherId(), 701, 10, adminCookie);
        group2Course1Teacher = createTestGroup(course1.getCourseId(), teacherEntity.getTeacherId(), 702, 10, adminCookie);
        group1Course2Teacher = createTestGroup(course2.getCourseId(), teacherEntity.getTeacherId(), 703, 10, adminCookie);

        mockMvc.perform(post("/api/enrollments/admin/groups/" + group1Course1Teacher.getGroupId() + "/students/" + studentEntity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isCreated());

        DefineMeetingDTO defineDto = new DefineMeetingDTO(1, LocalDateTime.now().plusDays(1), List.of("Test Meeting"), null);
        MvcResult meetingResult = mockMvc.perform(post("/api/groups/" + group1Course1Teacher.getGroupId() + "/meetings")
                        .cookie(teacherCookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(defineDto)))
                .andExpect(status().isCreated()).andReturn();
        List<MeetingResponse> meetings = objectMapper.readValue(meetingResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, MeetingResponse.class));
        meeting1Group1 = meetings.get(0);

        AttendanceRecordDTO attRec = new AttendanceRecordDTO(studentEntity.getStudentId(), AttendanceStatus.PRESENT.name());
        RecordAttendanceDTO recordAttDto = new RecordAttendanceDTO(List.of(attRec));
        mockMvc.perform(post("/api/meetings/" + meeting1Group1.getMeetingId() + "/attendance")
                        .cookie(teacherCookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recordAttDto)))
                .andExpect(status().isOk());

        CreateGradeDTO gradeDto = new CreateGradeDTO(studentEntity.getStudentId(), course1.getCourseId(), "4.5", "Good work on profile course 1");
        mockMvc.perform(post("/api/grades")
                        .cookie(teacherCookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(gradeDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void studentShouldGetTheirOwnProfile() throws Exception {
        mockMvc.perform(get("/api/students/me").cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentEntity.getStudentId()))
                .andExpect(jsonPath("$.indexNumber").value(studentEntity.getIndexNumber()))
                .andExpect(jsonPath("$.userInfo.username").value(STUDENT_EMAIL));
    }

    @Test
    void studentShouldGetTheirEnrolledGroups() throws Exception {
        mockMvc.perform(get("/api/students/me/groups").cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].groupId").value(group1Course1Teacher.getGroupId()))
                .andExpect(jsonPath("$[0].course.courseCode").value(course1.getCourseCode()));
    }

    @Test
    void studentShouldGetTheirGrades() throws Exception {
        mockMvc.perform(get("/api/students/me/grades").cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].gradeValue").value("4.5"))
                .andExpect(jsonPath("$[0].course.courseId").value(course1.getCourseId()));

        mockMvc.perform(get("/api/students/me/grades").cookie(studentCookie)
                        .param("courseId", course1.getCourseId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/students/me/grades").cookie(studentCookie)
                        .param("courseId", course2.getCourseId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void studentShouldGetTheirAttendance() throws Exception {
        mockMvc.perform(get("/api/students/me/attendance").cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value(AttendanceStatus.PRESENT.name()))
                .andExpect(jsonPath("$[0].meeting.meetingId").value(meeting1Group1.getMeetingId()));

        mockMvc.perform(get("/api/students/me/attendance").cookie(studentCookie)
                        .param("groupId", group1Course1Teacher.getGroupId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/students/me/attendance").cookie(studentCookie)
                        .param("courseId", course1.getCourseId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/students/me/attendance").cookie(studentCookie)
                        .param("meetingNumber", String.valueOf(meeting1Group1.getMeetingNumber())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

         mockMvc.perform(get("/api/students/me/attendance").cookie(studentCookie)
                        .param("courseId", course2.getCourseId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void teacherShouldGetTheirOwnProfile() throws Exception {
        mockMvc.perform(get("/api/teachers/me").cookie(teacherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId").value(teacherEntity.getTeacherId()))
                .andExpect(jsonPath("$.title").value(teacherEntity.getTitle()))
                .andExpect(jsonPath("$.userInfo.username").value(TEACHER_EMAIL));
    }

    @Test
    void teacherShouldGetTheirCourses() throws Exception {
        mockMvc.perform(get("/api/teachers/me/courses").cookie(teacherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.courseCode == '" + course1.getCourseCode() + "')]").exists())
                .andExpect(jsonPath("$[?(@.courseCode == '" + course2.getCourseCode() + "')]").exists());
    }

    @Test
    void teacherShouldGetTheirGroups() throws Exception {
        mockMvc.perform(get("/api/teachers/me/groups").cookie(teacherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.groupId == " + group1Course1Teacher.getGroupId() + ")]").exists())
                .andExpect(jsonPath("$[?(@.groupId == " + group2Course1Teacher.getGroupId() + ")]").exists())
                .andExpect(jsonPath("$[?(@.groupId == " + group1Course2Teacher.getGroupId() + ")]").exists());

        mockMvc.perform(get("/api/teachers/me/groups").cookie(teacherCookie)
                        .param("courseId", course1.getCourseId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.groupId == " + group1Course1Teacher.getGroupId() + " && @.course.courseId == " + course1.getCourseId() + ")]").exists())
                .andExpect(jsonPath("$[?(@.groupId == " + group2Course1Teacher.getGroupId() + " && @.course.courseId == " + course1.getCourseId() + ")]").exists());

        CourseResponse otherCourse = createTestCourse("OTHER-CRS", "Other Course", 1, adminCookie);
        mockMvc.perform(get("/api/teachers/me/groups").cookie(teacherCookie)
                        .param("courseId", otherCourse.getCourseId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }
}