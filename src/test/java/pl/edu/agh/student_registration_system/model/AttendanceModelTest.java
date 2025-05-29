package pl.edu.agh.student_registration_system.model;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class AttendanceModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private Meeting testMeeting;
    private Student testStudent;
    private Teacher testTeacher;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        User user1 = new User(null, "John", "Doe", "password", "john.doe@example.com", true, studentRole, null, null);
        entityManager.persist(user1);
        testStudent = new Student(null, "123456", user1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testStudent);

        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        User user2 = new User(null, "Jane", "Smith", "password", "jane.smith@example.com", true, teacherRole, null, null);
        entityManager.persist(user2);
        testTeacher = new Teacher(null, "Prof.", user2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testTeacher);

        Course testCourse = new Course(null, "Test Course", "TC101", "Description", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(testCourse);
        CourseGroup testGroup = new CourseGroup(null, 1, 30, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(testGroup);
        testMeeting = new Meeting(null, 1, java.time.LocalDateTime.now(), "Intro", testGroup, new HashSet<>());
        entityManager.persist(testMeeting);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindAttendance() {
        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, testMeeting, testStudent, testTeacher);
        Attendance savedAttendance = entityManager.persistAndFlush(attendance);

        assertThat(savedAttendance).isNotNull();
        assertThat(savedAttendance.getAttendanceId()).isNotNull();
        assertThat(savedAttendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);

        Attendance foundAttendance = entityManager.find(Attendance.class, savedAttendance.getAttendanceId());
        assertThat(foundAttendance).isEqualTo(savedAttendance);
        assertThat(foundAttendance.getMeeting().getMeetingId()).isEqualTo(testMeeting.getMeetingId());
        assertThat(foundAttendance.getStudent().getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(foundAttendance.getRecordedByTeacher().getTeacherId()).isEqualTo(testTeacher.getTeacherId());
    }

    @Test
    void testUniqueConstraintForMeetingAndStudent() {
        Attendance attendance1 = new Attendance(null, AttendanceStatus.PRESENT, testMeeting, testStudent, testTeacher);
        entityManager.persistAndFlush(attendance1);

        Attendance attendance2 = new Attendance(null, AttendanceStatus.ABSENT, testMeeting, testStudent, testTeacher);

        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(attendance2);
        });
    }

    @Test
    void testStatusIsNotNull() {
        Attendance attendance = new Attendance(null, null, testMeeting, testStudent, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(attendance);
        });
    }

    @Test
    void testMeetingIsNotNull() {
        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, null, testStudent, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(attendance);
        });
    }

    @Test
    void testStudentIsNotNull() {
        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, testMeeting, null, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(attendance);
        });
    }

    @Test
    void testRecordedByTeacherIsNotNull() {
        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, testMeeting, testStudent, null);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(attendance);
        });
    }
}