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
class EnrollmentModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private Student testStudent;
    private CourseGroup testCourseGroup;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        User studentUser = new User(null, "Marie", "Schrader", "pass", "m.schrader@example.com", true, studentRole, null, null);
        entityManager.persist(studentUser);
        testStudent = new Student(null, "789012", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testStudent);

        Course testCourse = new Course(null, "Chemistry", "CHEM101", "Intro to chem", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(testCourse);
        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        User teacherUser = new User(null, "Hank", "Schrader", "pass", "h.schrader@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        Teacher testTeacher = new Teacher(null, "Dr.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testTeacher);
        testCourseGroup = new CourseGroup(null, 1, 20, testCourse, testTeacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(testCourseGroup);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindEnrollment() {
        LocalDateTime enrollmentDate = LocalDateTime.now();
        Enrollment enrollment = new Enrollment(null, enrollmentDate, testStudent, testCourseGroup);
        Enrollment savedEnrollment = entityManager.persistAndFlush(enrollment);

        assertThat(savedEnrollment).isNotNull();
        assertThat(savedEnrollment.getEnrollmentId()).isNotNull();
        assertThat(savedEnrollment.getEnrollmentDate()).isEqualToIgnoringNanos(enrollmentDate);
        assertThat(savedEnrollment.getStudent().getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(savedEnrollment.getGroup().getCourseGroupId()).isEqualTo(testCourseGroup.getCourseGroupId());

        Enrollment foundEnrollment = entityManager.find(Enrollment.class, savedEnrollment.getEnrollmentId());
        assertThat(foundEnrollment).isEqualTo(savedEnrollment);
    }

    @Test
    void testStudentIsNotNull() {
        Enrollment enrollment = new Enrollment(null, LocalDateTime.now(), null, testCourseGroup);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(enrollment);
        });
    }

    @Test
    void testCourseGroupIsNotNull() {
        Enrollment enrollment = new Enrollment(null, LocalDateTime.now(), testStudent, null);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(enrollment);
        });
    }
}