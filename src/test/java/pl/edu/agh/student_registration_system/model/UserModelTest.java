package pl.edu.agh.student_registration_system.model;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private Role studentRole;
    private Role teacherRole;
    private Role deaneryRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);

        teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        deaneryRole = new Role(RoleType.DEANERY_STAFF);
        entityManager.persist(deaneryRole);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindUser() {
        User user = new User(null, "Albus", "Dumbledore", "pass123", "a.dumbledore@example.com", true, deaneryRole, null, null);
        User savedUser = entityManager.persistAndFlush(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("Albus");
        assertThat(savedUser.getLastName()).isEqualTo("Dumbledore");
        assertThat(savedUser.getEmail()).isEqualTo("a.dumbledore@example.com");
        assertThat(savedUser.getIsActive()).isTrue();
        assertThat(savedUser.getRole().getRoleName()).isEqualTo(RoleType.DEANERY_STAFF);

        User foundUser = entityManager.find(User.class, savedUser.getUserId());
        assertThat(foundUser).isEqualTo(savedUser);
    }

    @Test
    void testEmailUniqueConstraint() {
        User user1 = new User(null, "Ron", "Weasley", "pass", "r.weasley@example.com", true, studentRole, null, null);
        entityManager.persistAndFlush(user1);

        User user2 = new User(null, "Ginny", "Weasley", "anotherpass", "r.weasley@example.com", true, studentRole, null, null);
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }

    @Test
    void testRequiredFieldsNotNull() {
        assertThrows(ConstraintViolationException.class, () -> {
            User user = new User(null, null, "Doe", "pass", "test@example.com", true, studentRole, null, null);
            entityManager.persistAndFlush(user);
        });

        assertThrows(ConstraintViolationException.class, () -> {
            User user = new User(null, "John", null, "pass", "test2@example.com", true, studentRole, null, null);
            entityManager.persistAndFlush(user);
        });

        assertThrows(ConstraintViolationException.class, () -> {
            User user = new User(null, "John", "Doe", null, "test3@example.com", true, studentRole, null, null);
            entityManager.persistAndFlush(user);
        });

        assertThrows(ConstraintViolationException.class, () -> {
            User user = new User(null, "John", "Doe", "pass", null, true, studentRole, null, null);
            entityManager.persistAndFlush(user);
        });

        assertThrows(ConstraintViolationException.class, () -> {
            User user = new User(null, "John", "Doe", "pass", "test4@example.com", null, studentRole, null, null);
            entityManager.persistAndFlush(user);
        });

        assertThrows(ConstraintViolationException.class, () -> {
            User user = new User(null, "John", "Doe", "pass", "test5@example.com", true, null, null, null);
            entityManager.persistAndFlush(user);
        });
    }

    @Test
    void testOneToOneStudentProfileRelationship() {
        User user = new User(null, "Neville", "Longbottom", "pass", "n.longbottom@example.com", true, studentRole, null, null);
        Student student = new Student(null, "123000", user, new HashSet<>(), new HashSet<>(), new HashSet<>());
        user.setStudentProfile(student);

        entityManager.persistAndFlush(user);
        entityManager.clear();

        User foundUser = entityManager.find(User.class, user.getUserId());
        assertThat(foundUser.getStudentProfile()).isNotNull();
        assertThat(foundUser.getStudentProfile().getIndexNumber()).isEqualTo("123000");

        Long studentId = foundUser.getStudentProfile().getStudentId();
        entityManager.remove(foundUser);
        entityManager.flush();

        Student deletedStudent = entityManager.find(Student.class, studentId);
        assertThat(deletedStudent).isNull();
    }

    @Test
    void testOneToOneTeacherProfileRelationship() {
        User user = new User(null, "Minerva", "McGonagall", "pass", "m.mcgonagall@example.com", true, teacherRole, null, null);
        Teacher teacher = new Teacher(null, "Prof.", user, new HashSet<>(), new HashSet<>(), new HashSet<>());
        user.setTeacherProfile(teacher);

        entityManager.persistAndFlush(user);
        entityManager.clear();

        User foundUser = entityManager.find(User.class, user.getUserId());
        assertThat(foundUser.getTeacherProfile()).isNotNull();
        assertThat(foundUser.getTeacherProfile().getTitle()).isEqualTo("Prof.");

        Long teacherId = foundUser.getTeacherProfile().getTeacherId();
        entityManager.remove(foundUser);
        entityManager.flush();

        Teacher deletedTeacher = entityManager.find(Teacher.class, teacherId);
        assertThat(deletedTeacher).isNull();
    }
}