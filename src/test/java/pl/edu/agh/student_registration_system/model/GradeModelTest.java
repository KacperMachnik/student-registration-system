package pl.edu.agh.student_registration_system.model;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class GradeModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private Student testStudent;
    private Course testCourse;
    private Teacher testTeacher;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        User studentUser = new User(null, "Skyler", "White", "pass", "s.white@example.com", true, studentRole, null, null);
        entityManager.persist(studentUser);
        testStudent = new Student(null, "112233", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testStudent);

        testCourse = new Course(null, "Calculus", "CALC200", "Advanced math", 5, new HashSet<>(), new HashSet<>());
        entityManager.persist(testCourse);

        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        User teacherUser = new User(null, "Gus", "Fring", "pass", "g.fring@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        testTeacher = new Teacher(null, "Mr.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(testTeacher);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindGrade() {
        LocalDateTime gradeDate = LocalDateTime.now();
        Grade grade = new Grade(null, "A", gradeDate, "Excellent performance", testStudent, testCourse, testTeacher);
        Grade savedGrade = entityManager.persistAndFlush(grade);

        assertThat(savedGrade).isNotNull();
        assertThat(savedGrade.getGradeId()).isNotNull();
        assertThat(savedGrade.getGradeValue()).isEqualTo("A");
        assertThat(savedGrade.getGradeDate()).isEqualToIgnoringNanos(gradeDate);
        assertThat(savedGrade.getComment()).isEqualTo("Excellent performance");
        assertThat(savedGrade.getStudent().getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(savedGrade.getCourse().getCourseId()).isEqualTo(testCourse.getCourseId());
        assertThat(savedGrade.getTeacher().getTeacherId()).isEqualTo(testTeacher.getTeacherId());

        Grade foundGrade = entityManager.find(Grade.class, savedGrade.getGradeId());
        assertThat(foundGrade).isEqualTo(savedGrade);
    }

    @Test
    void testGradeValueIsNotNull() {
        Grade grade = new Grade(null, null, LocalDateTime.now(), "Comment", testStudent, testCourse, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(grade);
        });
    }

    @Test
    void testGradeDateIsNotNull() {
        Grade grade = new Grade(null, "B+", null, "Comment", testStudent, testCourse, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(grade);
        });
    }

    @Test
    void testStudentIsNotNull() {
        Grade grade = new Grade(null, "C", LocalDateTime.now(), "Comment", null, testCourse, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(grade);
        });
    }

    @Test
    void testCourseIsNotNull() {
        Grade grade = new Grade(null, "D", LocalDateTime.now(), "Comment", testStudent, null, testTeacher);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(grade);
        });
    }

    @Test
    void testTeacherIsNotNull() {
        Grade grade = new Grade(null, "F", LocalDateTime.now(), "Comment", testStudent, testCourse, null);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(grade);
        });
    }
}