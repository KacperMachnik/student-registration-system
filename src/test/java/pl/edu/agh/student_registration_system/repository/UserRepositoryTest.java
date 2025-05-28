package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);

        user1 = new User(null, "Test", "User1", "pass1", "user1@example.com", true, studentRole, null, null);
        userRepository.save(user1);

        User user2 = new User(null, "Another", "User2", "pass2", "user2@example.com", false, studentRole, null, null);
        userRepository.save(user2);

        entityManager.flush();
    }

    @Test
    void testFindByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("user1@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("Test");

        Optional<User> notFoundUser = userRepository.findByEmail("nonexistent@example.com");
        assertThat(notFoundUser).isNotPresent();
    }

    @Test
    void testExistsByEmail() {
        boolean exists = userRepository.existsByEmail("user1@example.com");
        assertThat(exists).isTrue();

        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByEmailWithRole() {
        Optional<User> foundUserWithRole = userRepository.findByEmailWithRole("user1@example.com");
        assertThat(foundUserWithRole).isPresent();
        assertThat(foundUserWithRole.get().getFirstName()).isEqualTo("Test");
        assertThat(foundUserWithRole.get().getRole()).isNotNull();
        assertThat(foundUserWithRole.get().getRole().getRoleName()).isEqualTo(RoleType.STUDENT);

        Optional<User> notFoundUser = userRepository.findByEmailWithRole("nonexistent@example.com");
        assertThat(notFoundUser).isNotPresent();
    }
}