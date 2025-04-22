package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role studentRole;
    private Role teacherRole;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setRoleName(RoleType.STUDENT);
        roleRepository.save(studentRole);

        teacherRole = new Role();
        teacherRole.setRoleName(RoleType.TEACHER);
        roleRepository.save(teacherRole);

        user1 = new User();
        user1.setEmail("student@example.com");
        user1.setPassword("password");
        user1.setFirstName("Jan");
        user1.setLastName("Kowalski");
        user1.setRole(studentRole);
        user1.setIsActive(true);
        userRepository.save(user1);

        user2 = new User();
        user2.setEmail("teacher@example.com");
        user2.setPassword("password");
        user2.setFirstName("Anna");
        user2.setLastName("Nowak");
        user2.setRole(teacherRole);
        user2.setIsActive(true);
        userRepository.save(user2);
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("student@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(user1, foundUser.get());
        assertEquals("Jan", foundUser.get().getFirstName());
        assertEquals("Kowalski", foundUser.get().getLastName());
    }

    @Test
    @DisplayName("Should not find user by non-existent email")
    void shouldNotFindUserByNonExistentEmail() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        boolean exists = userRepository.existsByEmail("student@example.com");
        assertTrue(exists);

        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should find user by email with role")
    void shouldFindUserByEmailWithRole() {
        Optional<User> foundUser = userRepository.findByEmailWithRole("teacher@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(user2, foundUser.get());
        assertEquals(teacherRole, foundUser.get().getRole());
        assertEquals(RoleType.TEACHER, foundUser.get().getRole().getRoleName());
    }

    @Test
    @DisplayName("Should save new user")
    void shouldSaveNewUser() {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password");
        newUser.setFirstName("Piotr");
        newUser.setLastName("Wiśniewski");
        newUser.setRole(studentRole);
        newUser.setIsActive(true);

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getUserId());

        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent());
        assertEquals("Piotr", foundUser.get().getFirstName());
        assertEquals("Wiśniewski", foundUser.get().getLastName());
    }

    @Test
    @DisplayName("Should update existing user")
    void shouldUpdateExistingUser() {
        user1.setFirstName("Janusz");
        user1.setLastName("Nowacki");

        User updatedUser = userRepository.save(user1);

        assertEquals(user1.getUserId(), updatedUser.getUserId());
        assertEquals("Janusz", updatedUser.getFirstName());
        assertEquals("Nowacki", updatedUser.getLastName());

        Optional<User> foundUser = userRepository.findById(user1.getUserId());
        assertTrue(foundUser.isPresent());
        assertEquals("Janusz", foundUser.get().getFirstName());
        assertEquals("Nowacki", foundUser.get().getLastName());
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        userRepository.delete(user2);

        Optional<User> foundUser = userRepository.findById(user2.getUserId());
        assertFalse(foundUser.isPresent());
    }
}
