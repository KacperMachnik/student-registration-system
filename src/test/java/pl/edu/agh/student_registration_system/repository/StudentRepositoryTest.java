package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.User;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    private User user1, user2;
    private Student student1;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);

        user1 = new User(null, "Student", "S1", "pass", "s1@test.com", true, studentRole, null, null);
        entityManager.persist(user1);
        student1 = new Student(null, "S00001", user1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        studentRepository.save(student1);

        user2 = new User(null, "Student", "S2", "pass", "s2@test.com", true, studentRole, null, null);
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void testExistsByIndexNumber() {
        boolean exists = studentRepository.existsByIndexNumber("S00001");
        assertThat(exists).isTrue();

        boolean notExists = studentRepository.existsByIndexNumber("S99999");
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByUser() {
        Optional<Student> foundStudent1 = studentRepository.findByUser(user1);
        assertThat(foundStudent1).isPresent();
        assertThat(foundStudent1.get()).isEqualTo(student1);

        Optional<Student> notFoundStudent = studentRepository.findByUser(user2);
        assertThat(notFoundStudent).isNotPresent();
    }

    @Test
    void testFindByIdWithUser() {
        Optional<Student> foundStudent1 = studentRepository.findByIdWithUser(student1.getStudentId());
        assertThat(foundStudent1).isPresent();
        assertThat(foundStudent1.get().getUser()).isEqualTo(user1);
        assertThat(foundStudent1.get().getUser().getRole()).isEqualTo(studentRole);

        Optional<Student> notFound = studentRepository.findByIdWithUser(999L);
        assertThat(notFound).isNotPresent();
    }
}