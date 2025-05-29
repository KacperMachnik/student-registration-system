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
class CourseGroupModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private Course testCourse;
    private Teacher testTeacher;

    @BeforeEach
    void setUp() {
        testCourse = new Course(null, "Database Systems", "DB202", "DB concepts", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(testCourse);

        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        User teacherUser = new User(null, "Saul", "Goodman", "pass", "s.goodman@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        testTeacher = new Teacher(null, "Prof.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testTeacher);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindCourseGroup() {
        CourseGroup courseGroup = new CourseGroup(null, 1, 25, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        CourseGroup savedCourseGroup = entityManager.persistAndFlush(courseGroup);

        assertThat(savedCourseGroup).isNotNull();
        assertThat(savedCourseGroup.getCourseGroupId()).isNotNull();
        assertThat(savedCourseGroup.getGroupNumber()).isEqualTo(1);
        assertThat(savedCourseGroup.getMaxCapacity()).isEqualTo(25);
        assertThat(savedCourseGroup.getCourse().getCourseId()).isEqualTo(testCourse.getCourseId());
        assertThat(savedCourseGroup.getTeacher().getTeacherId()).isEqualTo(testTeacher.getTeacherId());

        CourseGroup foundCourseGroup = entityManager.find(CourseGroup.class, savedCourseGroup.getCourseGroupId());
        assertThat(foundCourseGroup).isEqualTo(savedCourseGroup);
    }

    @Test
    void testUniqueConstraintForCourseAndGroupNumber() {
        CourseGroup group1 = new CourseGroup(null, 1, 20, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        entityManager.persistAndFlush(group1);

        CourseGroup group2 = new CourseGroup(null, 1, 25, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(group2);
        });
    }

    @Test
    void testGroupNumberIsNotNull() {
        CourseGroup courseGroup = new CourseGroup(null, null, 20, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(courseGroup);
        });
    }

    @Test
    void testMaxCapacityIsNotNull() {
        CourseGroup courseGroup = new CourseGroup(null, 1, null, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(courseGroup);
        });
    }

    @Test
    void testCourseIsNotNull() {
        CourseGroup courseGroup = new CourseGroup(null, 1, 20, null, testTeacher, new HashSet<>(), new ArrayList<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(courseGroup);
        });
    }

    @Test
    void testOneToManyEnrollmentsRelationship() {
        CourseGroup courseGroup = new CourseGroup(null, 1, 30, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(courseGroup);

        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        User studentUser = new User(null, "Jesse", "Pinkman", "pass", "j.pinkman@example.com", true, studentRole, null, null);
        entityManager.persist(studentUser);
        Student student = new Student(null, "654321", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        Enrollment enrollment = new Enrollment(null, java.time.LocalDateTime.now(), student, courseGroup);
        courseGroup.getEnrollments().add(enrollment);
        entityManager.persistAndFlush(enrollment);
        entityManager.clear();

        CourseGroup foundGroup = entityManager.find(CourseGroup.class, courseGroup.getCourseGroupId());
        assertThat(foundGroup.getEnrollments()).hasSize(1);
        assertThat(foundGroup.getEnrollments().iterator().next().getStudent().getStudentId()).isEqualTo(student.getStudentId());

        foundGroup.getEnrollments().clear();
        entityManager.flush();
        entityManager.clear();
        foundGroup = entityManager.find(CourseGroup.class, courseGroup.getCourseGroupId());
        assertThat(foundGroup.getEnrollments()).isEmpty();
    }

    @Test
    void testOneToManyMeetingsRelationship() {
        CourseGroup courseGroup = new CourseGroup(null, 2, 20, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(courseGroup);

        Meeting meeting1 = new Meeting(null, 1, java.time.LocalDateTime.now().minusDays(1), "Topic 1", courseGroup, new HashSet<>());
        Meeting meeting2 = new Meeting(null, 2, java.time.LocalDateTime.now(), "Topic 2", courseGroup, new HashSet<>());

        courseGroup.getMeetings().add(meeting1);
        courseGroup.getMeetings().add(meeting2);

        entityManager.persistAndFlush(meeting1);
        entityManager.persistAndFlush(meeting2);
        entityManager.clear();

        CourseGroup foundGroup = entityManager.find(CourseGroup.class, courseGroup.getCourseGroupId());
        assertThat(foundGroup.getMeetings()).hasSize(2);

        Meeting meetingToRemove = foundGroup.getMeetings().stream()
                .filter(m -> m.getMeetingNumber().equals(1))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Meeting to remove not found in collection with meetingNumber=1"));

        boolean removedFromList = foundGroup.getMeetings().remove(meetingToRemove);
        assertThat(removedFromList).isTrue();

        entityManager.flush();
        entityManager.clear();

        foundGroup = entityManager.find(CourseGroup.class, courseGroup.getCourseGroupId());
        assertThat(foundGroup.getMeetings()).hasSize(1);
        assertThat(foundGroup.getMeetings().get(0).getMeetingNumber()).isEqualTo(2);
    }
}