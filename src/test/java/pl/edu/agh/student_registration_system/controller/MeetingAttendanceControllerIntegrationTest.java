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
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeetingAttendanceControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseGroupRepository courseGroupRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private MeetingRepository meetingRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PasswordEncoder passwordEncoder;


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
            User existingUser = userRepository.findByEmail(email).get();
            if (roleType == RoleType.TEACHER && existingUser.getTeacherProfile() == null) {
                Teacher teacher = new Teacher();
                teacher.setTitle(titleIfTeacher);
                teacher.setUser(existingUser);
                existingUser.setTeacherProfile(teacherRepository.save(teacher));
                userRepository.save(existingUser);
            } else if (roleType == RoleType.STUDENT && existingUser.getStudentProfile() == null) {
                 Student student = new Student();
                 String indexNumber;
                 do { indexNumber = String.valueOf(100000 + (int)(Math.random() * 900000)); }
                 while (studentRepository.existsByIndexNumber(indexNumber));
                 student.setIndexNumber(indexNumber);
                 student.setUser(existingUser);
                 existingUser.setStudentProfile(studentRepository.save(student));
                 userRepository.save(existingUser);
            }
            return existingUser;
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
             do { indexNumber = String.valueOf(100000 + (int)(Math.random() * 900000)); }
             while (studentRepository.existsByIndexNumber(indexNumber));
             student.setIndexNumber(indexNumber);
             student.setUser(user);
             user.setStudentProfile(student);
        }
        return userRepository.save(user);
    }


    @BeforeEach
     void setUp() throws Exception {
        adminCookie = loginAndGetCookie(ADMIN_EMAIL, ADMIN_PASSWORD, RoleType.DEANERY_STAFF);
        teacher1Cookie = loginAndGetCookie(TEACHER_EMAIL, TEACHER_PASSWORD, RoleType.TEACHER);

        User teacher2User = createAndSaveNewUserWithRole("teacher2.meeting@example.com", "Anna", "InnaNauczycielka", "teacher2pass", RoleType.TEACHER, "Dr");
        teacher2Cookie = loginAndGetCookie(teacher2User.getEmail(), "teacher2pass", RoleType.TEACHER);
        teacher2Entity = teacherRepository.findByUser(teacher2User)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile for user", "email", teacher2User.getEmail()));

        studentCookie = loginAndGetCookie(STUDENT_EMAIL, STUDENT_PASSWORD, RoleType.STUDENT);
        User studentUserEntity = userRepository.findByEmail(STUDENT_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", STUDENT_EMAIL));
        studentEntity = studentRepository.findByUser(studentUserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile for user", "email", STUDENT_EMAIL));

        User teacher1UserEntity = userRepository.findByEmail(TEACHER_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", TEACHER_EMAIL));
        teacher1Entity = teacherRepository.findByUser(teacher1UserEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile for user", "email", TEACHER_EMAIL));

        testCourse = createTestCourse("MEET-C1", "Meetings Course", 3, adminCookie);
        testGroup = createTestGroup(testCourse.getCourseId(), teacher1Entity.getTeacherId(), 601, 10, adminCookie);

        mockMvc.perform(post("/api/enrollments/admin/groups/" + testGroup.getGroupId() + "/students/" + studentEntity.getStudentId())
                        .cookie(adminCookie))
                .andExpect(status().isCreated());
    }

    private DefineMeetingDTO createDefineMeetingDTO(int numberOfMeetings, LocalDateTime firstDateTime, List<String> topics) {
        DefineMeetingDTO dto = new DefineMeetingDTO();
        dto.setNumberOfMeetings(numberOfMeetings);
        dto.setFirstMeetingDateTime(firstDateTime);
        dto.setTopics(topics);
        return dto;
    }

    @Test
    void teacherOfGroupShouldDefineMeetings() throws Exception {
        DefineMeetingDTO defineDto = createDefineMeetingDTO(2, LocalDateTime.now().plusDays(1), List.of("Topic 1", "Topic 2"));

        mockMvc.perform(post("/api/groups/" + testGroup.getGroupId() + "/meetings")
                        .cookie(teacher1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defineDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].meetingNumber").value(1))
                .andExpect(jsonPath("$[0].topic").value("Topic 1"))
                .andExpect(jsonPath("$[1].meetingNumber").value(2))
                .andExpect(jsonPath("$[1].topic").value("Topic 2"));

        assertThat(meetingRepository.findByGroupOrderByMeetingNumber(courseGroupRepository.findById(testGroup.getGroupId()).get())).hasSize(2);
    }

    @Test
    void deaneryStaffShouldDefineMeetings() throws Exception {
        DefineMeetingDTO defineDto = createDefineMeetingDTO(1, LocalDateTime.now().plusDays(2), List.of("Admin Topic"));

        mockMvc.perform(post("/api/groups/" + testGroup.getGroupId() + "/meetings")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defineDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topic").value("Admin Topic"));
    }

    @Test
    void studentShouldNotDefineMeetings() throws Exception {
        DefineMeetingDTO defineDto = createDefineMeetingDTO(1, LocalDateTime.now().plusDays(3), List.of("Student Topic"));
        mockMvc.perform(post("/api/groups/" + testGroup.getGroupId() + "/meetings")
                        .cookie(studentCookie))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void otherTeacherShouldNotDefineMeetingsForGroup() throws Exception {
        DefineMeetingDTO defineDto = createDefineMeetingDTO(1, LocalDateTime.now().plusDays(4), List.of("Other Teacher Topic"));
        mockMvc.perform(post("/api/groups/" + testGroup.getGroupId() + "/meetings")
                        .cookie(teacher2Cookie))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void authorizedUsersShouldGetMeetingsForGroup() throws Exception {
        DefineMeetingDTO defineDto = createDefineMeetingDTO(1, LocalDateTime.now().plusDays(5), List.of("Meeting to Get"));
        mockMvc.perform(post("/api/groups/" + testGroup.getGroupId() + "/meetings")
                        .cookie(teacher1Cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(defineDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/groups/" + testGroup.getGroupId() + "/meetings").cookie(teacher1Cookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/groups/" + testGroup.getGroupId() + "/meetings").cookie(studentCookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
                
        mockMvc.perform(get("/api/groups/" + testGroup.getGroupId() + "/meetings").cookie(adminCookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }


    private MeetingResponse defineOneMeeting(Long groupId, Cookie cookie, String topic) throws Exception {
        DefineMeetingDTO defineDto = createDefineMeetingDTO(1, LocalDateTime.now().plusDays(10), List.of(topic));
        MvcResult result = mockMvc.perform(post("/api/groups/" + groupId + "/meetings")
                        .cookie(cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(defineDto)))
                .andExpect(status().isCreated()).andReturn();
        List<MeetingResponse> meetings = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, MeetingResponse.class));
        return meetings.get(0);
    }

    @Test
    void teacherOfGroupShouldRecordAttendance() throws Exception {
        MeetingResponse meeting = defineOneMeeting(testGroup.getGroupId(), teacher1Cookie, "Attendance Recording");

        AttendanceRecordDTO attRecord = new AttendanceRecordDTO(studentEntity.getStudentId(), AttendanceStatus.PRESENT.name());
        RecordAttendanceDTO recordDto = new RecordAttendanceDTO(List.of(attRecord));

        mockMvc.perform(post("/api/meetings/" + meeting.getMeetingId() + "/attendance")
                        .cookie(teacher1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].student.studentId").value(studentEntity.getStudentId()))
                .andExpect(jsonPath("$[0].status").value(AttendanceStatus.PRESENT.name()))
                .andExpect(jsonPath("$[0].recordedByTeacher.teacherId").value(teacher1Entity.getTeacherId()));

        assertThat(attendanceRepository.findByMeetingAndStudent(
                meetingRepository.findById(meeting.getMeetingId()).get(), studentEntity)
        ).isPresent();
    }
    
    @Test
    void studentOrOtherTeacherShouldNotRecordAttendance() throws Exception {
        MeetingResponse meeting = defineOneMeeting(testGroup.getGroupId(), teacher1Cookie, "No Record Topic");
        RecordAttendanceDTO recordDto = new RecordAttendanceDTO(List.of(new AttendanceRecordDTO(studentEntity.getStudentId(), "PRESENT")));

        mockMvc.perform(post("/api/meetings/" + meeting.getMeetingId() + "/attendance")
                        .cookie(studentCookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/meetings/" + meeting.getMeetingId() + "/attendance")
                        .cookie(teacher2Cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizedUsersShouldGetAttendanceForMeeting() throws Exception {
        MeetingResponse meeting = defineOneMeeting(testGroup.getGroupId(), teacher1Cookie, "Get Attendance Topic");
        AttendanceRecordDTO attRecord = new AttendanceRecordDTO(studentEntity.getStudentId(), AttendanceStatus.ABSENT.name());
        RecordAttendanceDTO recordDto = new RecordAttendanceDTO(List.of(attRecord));
        mockMvc.perform(post("/api/meetings/" + meeting.getMeetingId() + "/attendance")
                        .cookie(teacher1Cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/meetings/" + meeting.getMeetingId() + "/attendance").cookie(teacher1Cookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
                
        mockMvc.perform(get("/api/meetings/" + meeting.getMeetingId() + "/attendance").cookie(adminCookie))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

         mockMvc.perform(get("/api/meetings/" + meeting.getMeetingId() + "/attendance").cookie(studentCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void recordingTeacherShouldUpdateSingleAttendance() throws Exception {
        MeetingResponse meeting = defineOneMeeting(testGroup.getGroupId(), teacher1Cookie, "Update Attendance Topic");
        
        AttendanceRecordDTO attRecord = new AttendanceRecordDTO(studentEntity.getStudentId(), AttendanceStatus.PRESENT.name());
        RecordAttendanceDTO recordDto = new RecordAttendanceDTO(List.of(attRecord));
        MvcResult recordResult = mockMvc.perform(post("/api/meetings/" + meeting.getMeetingId() + "/attendance")
                        .cookie(teacher1Cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isOk()).andReturn();
        List<AttendanceResponse> recordedAttendances = objectMapper.readValue(recordResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, AttendanceResponse.class));
        Long attendanceId = recordedAttendances.get(0).getAttendanceId();

        UpdateAttendanceDTO updateDto = new UpdateAttendanceDTO(AttendanceStatus.EXCUSED.name());
        mockMvc.perform(put("/api/attendance/" + attendanceId)
                        .cookie(teacher1Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AttendanceStatus.EXCUSED.name()));
    }

    @Test
    void otherTeacherShouldNotUpdateSingleAttendance() throws Exception {
        MeetingResponse meeting = defineOneMeeting(testGroup.getGroupId(), teacher1Cookie, "No Update Topic");
        AttendanceRecordDTO attRecord = new AttendanceRecordDTO(studentEntity.getStudentId(), AttendanceStatus.PRESENT.name());
        RecordAttendanceDTO recordDto = new RecordAttendanceDTO(List.of(attRecord));
        MvcResult recordResult = mockMvc.perform(post("/api/meetings/" + meeting.getMeetingId() + "/attendance")
                        .cookie(teacher1Cookie).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recordDto)))
                .andExpect(status().isOk()).andReturn();
        List<AttendanceResponse> recordedAttendances = objectMapper.readValue(recordResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, AttendanceResponse.class));
        Long attendanceId = recordedAttendances.get(0).getAttendanceId();

        UpdateAttendanceDTO updateDto = new UpdateAttendanceDTO(AttendanceStatus.ABSENT.name());
        mockMvc.perform(put("/api/attendance/" + attendanceId)
                        .cookie(teacher2Cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}