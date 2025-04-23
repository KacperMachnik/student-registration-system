package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseGroupRepository courseGroupRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Student student1;
    private Student student2;
    private Meeting meeting1;
    private Meeting meeting2;
    private Attendance attendance1;
    private Attendance attendance2;
    private Teacher teacher;
    private Role studentRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        roleRepository.save(studentRole);

        teacherRole = new Role(RoleType.TEACHER);
        roleRepository.save(teacherRole);

        User user1 = new User();
        user1.setEmail("student1@example.com");
        user1.setPassword("password");
        user1.setFirstName("Jan");
        user1.setLastName("Kowalski");
        user1.setRole(studentRole);
        user1.setIsActive(true);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("student2@example.com");
        user2.setPassword("password");
        user2.setFirstName("Anna");
        user2.setLastName("Nowak");
        user2.setRole(studentRole);
        user2.setIsActive(true);
        userRepository.save(user2);

        User teacherUser = new User();
        teacherUser.setEmail("teacher@example.com");
        teacherUser.setPassword("password");
        teacherUser.setFirstName("Profesor");
        teacherUser.setLastName("Przykładowy");
        teacherUser.setRole(teacherRole);
        teacherUser.setIsActive(true);
        userRepository.save(teacherUser);

        student1 = new Student();
        student1.setUser(user1);
        student1.setIndexNumber("123456");
        studentRepository.save(student1);

        student2 = new Student();
        student2.setUser(user2);
        student2.setIndexNumber("654321");
        studentRepository.save(student2);

        teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacherRepository.save(teacher);

        Course course = new Course();
        course.setCourseName("Programowanie");
        course.setCourseCode("PRG101");
        course.setDescription("Kurs programowania");
        course.setCredits(5);
        courseRepository.save(course);

        CourseGroup courseGroup = new CourseGroup();
        courseGroup.setCourse(course);
        courseGroup.setTeacher(teacher);
        courseGroup.setGroupNumber(1);
        courseGroup.setMaxCapacity(30);
        courseGroupRepository.save(courseGroup);

        meeting1 = new Meeting();
        meeting1.setGroup(courseGroup);
        meeting1.setMeetingNumber(1);
        meeting1.setMeetingDate(LocalDateTime.now().plusDays(1));
        meeting1.setTopic("Wprowadzenie");
        meetingRepository.save(meeting1);

        meeting2 = new Meeting();
        meeting2.setGroup(courseGroup);
        meeting2.setMeetingNumber(2);
        meeting2.setMeetingDate(LocalDateTime.now().plusDays(2));
        meeting2.setTopic("Zaawansowane tematy");
        meetingRepository.save(meeting2);

        attendance1 = new Attendance();
        attendance1.setStudent(student1);
        attendance1.setMeeting(meeting1);
        attendance1.setStatus(AttendanceStatus.PRESENT);
        attendance1.setRecordedByTeacher(teacher);
        attendanceRepository.save(attendance1);

        attendance2 = new Attendance();
        attendance2.setStudent(student2);
        attendance2.setMeeting(meeting1);
        attendance2.setStatus(AttendanceStatus.ABSENT);
        attendance2.setRecordedByTeacher(teacher);
        attendanceRepository.save(attendance2);
    }

    @Test
    @DisplayName("Should find attendance by meeting and student")
    void shouldFindByMeetingAndStudent() {
        Optional<Attendance> foundAttendance = attendanceRepository.findByMeetingAndStudent(meeting1, student1);

        assertTrue(foundAttendance.isPresent());
        assertEquals(attendance1.getAttendanceId(), foundAttendance.get().getAttendanceId());
        assertEquals(AttendanceStatus.PRESENT, foundAttendance.get().getStatus());
    }

    @Test
    @DisplayName("Should return empty optional when attendance not found")
    void shouldReturnEmptyOptionalWhenAttendanceNotFound() {
        Optional<Attendance> foundAttendance = attendanceRepository.findByMeetingAndStudent(meeting2, student1);

        assertFalse(foundAttendance.isPresent());
    }

    @Test
    @DisplayName("Should find all attendances with specification for present status")
    void shouldFindAllWithSpecificationForPresentStatus() {
        Specification<Attendance> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), AttendanceStatus.PRESENT);

        List<Attendance> attendances = attendanceRepository.findAll(spec);

        assertEquals(1, attendances.size());
        assertEquals(attendance1.getAttendanceId(), attendances.get(0).getAttendanceId());
        assertEquals(AttendanceStatus.PRESENT, attendances.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find all attendances with specification for absent status")
    void shouldFindAllWithSpecificationForAbsentStatus() {
        Specification<Attendance> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), AttendanceStatus.ABSENT);

        List<Attendance> attendances = attendanceRepository.findAll(spec);

        assertEquals(1, attendances.size());
        assertEquals(attendance2.getAttendanceId(), attendances.get(0).getAttendanceId());
        assertEquals(AttendanceStatus.ABSENT, attendances.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find all attendances for specific meeting")
    void shouldFindAllForSpecificMeeting() {
        Specification<Attendance> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("meeting"), meeting1);

        List<Attendance> attendances = attendanceRepository.findAll(spec);

        assertEquals(2, attendances.size());
    }

    @Test
    @DisplayName("Should find all attendances for specific student")
    void shouldFindAllForSpecificStudent() {
        Specification<Attendance> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("student"), student1);

        List<Attendance> attendances = attendanceRepository.findAll(spec);

        assertEquals(1, attendances.size());
        assertEquals(student1.getStudentId(), attendances.get(0).getStudent().getStudentId());
    }

    @Test
    @DisplayName("Should save new attendance")
    void shouldSaveNewAttendance() {
        Attendance newAttendance = new Attendance();
        newAttendance.setStudent(student2);
        newAttendance.setMeeting(meeting2);
        newAttendance.setStatus(AttendanceStatus.PRESENT);
        newAttendance.setRecordedByTeacher(teacher);

        Attendance savedAttendance = attendanceRepository.save(newAttendance);

        assertNotNull(savedAttendance.getAttendanceId());

        Optional<Attendance> foundAttendance = attendanceRepository.findByMeetingAndStudent(meeting2, student2);
        assertTrue(foundAttendance.isPresent());
        assertEquals(AttendanceStatus.PRESENT, foundAttendance.get().getStatus());
    }

    @Test
    @DisplayName("Should update existing attendance")
    void shouldUpdateExistingAttendance() {
        attendance1.setStatus(AttendanceStatus.ABSENT);

        Attendance updatedAttendance = attendanceRepository.save(attendance1);

        assertEquals(attendance1.getAttendanceId(), updatedAttendance.getAttendanceId());
        assertEquals(AttendanceStatus.ABSENT, updatedAttendance.getStatus());

        Optional<Attendance> foundAttendance = attendanceRepository.findById(attendance1.getAttendanceId());
        assertTrue(foundAttendance.isPresent());
        assertEquals(AttendanceStatus.ABSENT, foundAttendance.get().getStatus());
    }

    @Test
    @DisplayName("Should delete attendance")
    void shouldDeleteAttendance() {
        attendanceRepository.delete(attendance1);

        Optional<Attendance> foundAttendance = attendanceRepository.findById(attendance1.getAttendanceId());
        assertFalse(foundAttendance.isPresent());
    }
}
