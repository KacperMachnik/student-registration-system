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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class MeetingModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private CourseGroup testCourseGroup;
    private Role studentRole;
    private Role teacherRole;


    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        Course testCourse = new Course(null, "Physics", "PHYS101", "Physics intro", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(testCourse);

        User teacherUserForGroup = new User(null, "Mike", "Ehrmantraut", "pass", "m.ehrmantraut@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUserForGroup);
        Teacher groupTeacher = new Teacher(null, "Prof.", teacherUserForGroup, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(groupTeacher);
        testCourseGroup = new CourseGroup(null, 1, 20, testCourse, groupTeacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(testCourseGroup);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindMeeting() {
        LocalDateTime meetingDate = LocalDateTime.now();
        Meeting meeting = new Meeting(null, 1, meetingDate, "Lecture 1", testCourseGroup, new HashSet<>());
        Meeting savedMeeting = entityManager.persistAndFlush(meeting);

        assertThat(savedMeeting).isNotNull();
        assertThat(savedMeeting.getMeetingId()).isNotNull();
        assertThat(savedMeeting.getMeetingNumber()).isEqualTo(1);
        assertThat(savedMeeting.getMeetingDate()).isEqualToIgnoringNanos(meetingDate);
        assertThat(savedMeeting.getTopic()).isEqualTo("Lecture 1");
        assertThat(savedMeeting.getGroup().getCourseGroupId()).isEqualTo(testCourseGroup.getCourseGroupId());

        Meeting foundMeeting = entityManager.find(Meeting.class, savedMeeting.getMeetingId());
        assertThat(foundMeeting).isEqualTo(savedMeeting);
    }

    @Test
    void testUniqueConstraintForGroupAndMeetingNumber() {
        Meeting meeting1 = new Meeting(null, 1, LocalDateTime.now().minusHours(1), "Topic A", testCourseGroup, new HashSet<>());
        entityManager.persistAndFlush(meeting1);

        Meeting meeting2 = new Meeting(null, 1, LocalDateTime.now(), "Topic B", testCourseGroup, new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(meeting2);
        });
    }

    @Test
    void testMeetingNumberIsNotNull() {
        Meeting meeting = new Meeting(null, null, LocalDateTime.now(), "Topic", testCourseGroup, new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(meeting);
        });
    }

    @Test
    void testMeetingDateIsNotNull() {
        Meeting meeting = new Meeting(null, 1, null, "Topic", testCourseGroup, new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(meeting);
        });
    }

    @Test
    void testCourseGroupIsNotNull() {
        Meeting meeting = new Meeting(null, 1, LocalDateTime.now(), "Topic", null, new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(meeting);
        });
    }

    @Test
    void testOneToManyAttendanceRecordsRelationship() {
        Meeting meeting = new Meeting(null, 1, LocalDateTime.now(), "Test Meeting", testCourseGroup, new HashSet<>());
        entityManager.persist(meeting);

        User studentUser = new User(null, "Todd", "Alquist", "pass", "t.alquist@example.com", true, studentRole, null, null);
        entityManager.persist(studentUser);
        Student student = new Student(null, "334455", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        User attendanceTeacherUser = new User(null, "Lydia", "Rodarte-Quayle", "passL", "l.rodarte@example.com", true, teacherRole, null, null);
        entityManager.persist(attendanceTeacherUser);
        Teacher attendanceTeacher = new Teacher(null, "Ms.", attendanceTeacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(attendanceTeacher);

        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, meeting, student, attendanceTeacher);
        meeting.getAttendanceRecords().add(attendance);
        entityManager.persistAndFlush(attendance);
        entityManager.clear();

        Meeting foundMeeting = entityManager.find(Meeting.class, meeting.getMeetingId());
        assertThat(foundMeeting.getAttendanceRecords()).hasSize(1);
        assertThat(foundMeeting.getAttendanceRecords().iterator().next().getStudent().getStudentId()).isEqualTo(student.getStudentId());
        assertThat(foundMeeting.getAttendanceRecords().iterator().next().getRecordedByTeacher().getTeacherId()).isEqualTo(attendanceTeacher.getTeacherId());


        foundMeeting.getAttendanceRecords().clear();
        entityManager.flush();
        entityManager.clear();
        foundMeeting = entityManager.find(Meeting.class, meeting.getMeetingId());
        assertThat(foundMeeting.getAttendanceRecords()).isEmpty();
    }
}