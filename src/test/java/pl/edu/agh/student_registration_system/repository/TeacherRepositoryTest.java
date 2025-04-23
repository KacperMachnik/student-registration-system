package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role teacherRole;
    private User user1;
    private User user2;
    private Teacher teacher1;
    private Teacher teacher2;

    @BeforeEach
    void setUp() {
        teacherRole = new Role();
        teacherRole.setRoleName(RoleType.TEACHER);
        roleRepository.save(teacherRole);

        user1 = new User();
        user1.setEmail("teacher1@example.com");
        user1.setPassword("password");
        user1.setFirstName("Jan");
        user1.setLastName("Profesor");
        user1.setRole(teacherRole);
        user1.setIsActive(true);
        userRepository.save(user1);

        user2 = new User();
        user2.setEmail("teacher2@example.com");
        user2.setPassword("password");
        user2.setFirstName("Anna");
        user2.setLastName("Doktor");
        user2.setRole(teacherRole);
        user2.setIsActive(true);
        userRepository.save(user2);

        teacher1 = new Teacher();
        teacher1.setUser(user1);
        teacher1.setTitle("Prof.");
        teacherRepository.save(teacher1);

        teacher2 = new Teacher();
        teacher2.setUser(user2);
        teacher2.setTitle("Dr");
        teacherRepository.save(teacher2);
    }

    @Test
    @DisplayName("Should find teacher by user")
    void shouldFindTeacherByUser() {
        Optional<Teacher> foundTeacher = teacherRepository.findByUser(user1);

        assertTrue(foundTeacher.isPresent());
        assertEquals(teacher1, foundTeacher.get());
        assertEquals("Prof.", foundTeacher.get().getTitle());
    }

    @Test
    @DisplayName("Should not find teacher by non-existent user")
    void shouldNotFindTeacherByNonExistentUser() {
        User nonExistentUser = new User();
        nonExistentUser.setUserId(999L);

        Optional<Teacher> foundTeacher = teacherRepository.findByUser(nonExistentUser);

        assertFalse(foundTeacher.isPresent());
    }

    @Test
    @DisplayName("Should find all teachers with pagination")
    void shouldFindAllTeachersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Teacher> teacherPage = teacherRepository.findAll(pageable);

        assertEquals(2, teacherPage.getTotalElements());
        assertEquals(2, teacherPage.getContent().size());
        assertTrue(teacherPage.getContent().contains(teacher1));
        assertTrue(teacherPage.getContent().contains(teacher2));
    }

    @Test
    @DisplayName("Should find teachers with specification")
    void shouldFindTeachersWithSpecification() {
        Specification<Teacher> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("title"), "Prof.");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Teacher> teacherPage = teacherRepository.findAll(spec, pageable);

        assertEquals(1, teacherPage.getTotalElements());
        assertEquals(teacher1, teacherPage.getContent().get(0));
    }

    @Test
    @DisplayName("Should save new teacher")
    void shouldSaveNewTeacher() {
        User newUser = new User();
        newUser.setEmail("newteacher@example.com");
        newUser.setPassword("password");
        newUser.setFirstName("Piotr");
        newUser.setLastName("Docent");
        newUser.setRole(teacherRole);
        newUser.setIsActive(true);
        userRepository.save(newUser);

        Teacher newTeacher = new Teacher();
        newTeacher.setUser(newUser);
        newTeacher.setTitle("Doc.");

        Teacher savedTeacher = teacherRepository.save(newTeacher);

        assertNotNull(savedTeacher.getTeacherId());

        Optional<Teacher> foundTeacher = teacherRepository.findById(savedTeacher.getTeacherId());
        assertTrue(foundTeacher.isPresent());
        assertEquals("Doc.", foundTeacher.get().getTitle());
        assertEquals("Piotr", foundTeacher.get().getUser().getFirstName());
    }

    @Test
    @DisplayName("Should update existing teacher")
    void shouldUpdateExistingTeacher() {
        teacher1.setTitle("Prof. dr hab.");

        Teacher updatedTeacher = teacherRepository.save(teacher1);

        assertEquals(teacher1.getTeacherId(), updatedTeacher.getTeacherId());
        assertEquals("Prof. dr hab.", updatedTeacher.getTitle());

        Optional<Teacher> foundTeacher = teacherRepository.findById(teacher1.getTeacherId());
        assertTrue(foundTeacher.isPresent());
        assertEquals("Prof. dr hab.", foundTeacher.get().getTitle());
    }

    @Test
    @DisplayName("Should delete teacher")
    void shouldDeleteTeacher() {
        teacherRepository.delete(teacher2);

        Optional<Teacher> foundTeacher = teacherRepository.findById(teacher2.getTeacherId());
        assertFalse(foundTeacher.isPresent());
    }
}
