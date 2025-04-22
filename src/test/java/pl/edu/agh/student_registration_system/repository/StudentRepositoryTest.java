package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role studentRole;
    private User user1;
    private User user2;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setRoleName(RoleType.STUDENT);
        roleRepository.save(studentRole);

        user1 = new User();
        user1.setEmail("student1@example.com");
        user1.setPassword("password");
        user1.setFirstName("Jan");
        user1.setLastName("Kowalski");
        user1.setRole(studentRole);
        user1.setIsActive(true);
        userRepository.save(user1);

        user2 = new User();
        user2.setEmail("student2@example.com");
        user2.setPassword("password");
        user2.setFirstName("Anna");
        user2.setLastName("Nowak");
        user2.setRole(studentRole);
        user2.setIsActive(true);
        userRepository.save(user2);

        student1 = new Student();
        student1.setUser(user1);
        student1.setIndexNumber("123456");
        studentRepository.save(student1);

        student2 = new Student();
        student2.setUser(user2);
        student2.setIndexNumber("654321");
        studentRepository.save(student2);
    }

    @Test
    @DisplayName("Should check if student exists by index number")
    void shouldCheckIfStudentExistsByIndexNumber() {
        boolean exists = studentRepository.existsByIndexNumber("123456");
        assertTrue(exists);

        boolean notExists = studentRepository.existsByIndexNumber("999999");
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should find student by user")
    void shouldFindStudentByUser() {
        Optional<Student> foundStudent = studentRepository.findByUser(user1);

        assertTrue(foundStudent.isPresent());
        assertEquals(student1, foundStudent.get());
        assertEquals("123456", foundStudent.get().getIndexNumber());
    }

    @Test
    @DisplayName("Should not find student by non-existent user")
    void shouldNotFindStudentByNonExistentUser() {
        User nonExistentUser = new User();
        nonExistentUser.setUserId(999L);

        Optional<Student> foundStudent = studentRepository.findByUser(nonExistentUser);

        assertFalse(foundStudent.isPresent());
    }

    @Test
    @DisplayName("Should find student by id with user")
    void shouldFindStudentByIdWithUser() {
        Optional<Student> foundStudent = studentRepository.findByIdWithUser(student1.getStudentId());

        assertTrue(foundStudent.isPresent());
        assertEquals(student1, foundStudent.get());
        assertEquals(user1, foundStudent.get().getUser());
        assertEquals(studentRole, foundStudent.get().getUser().getRole());
    }

    @Test
    @DisplayName("Should save new student")
    void shouldSaveNewStudent() {
        User newUser = new User();
        newUser.setEmail("newstudent@example.com");
        newUser.setPassword("password");
        newUser.setFirstName("Piotr");
        newUser.setLastName("Wiśniewski");
        newUser.setRole(studentRole);
        newUser.setIsActive(true);
        userRepository.save(newUser);

        Student newStudent = new Student();
        newStudent.setUser(newUser);
        newStudent.setIndexNumber("789012");

        Student savedStudent = studentRepository.save(newStudent);

        assertNotNull(savedStudent.getStudentId());

        Optional<Student> foundStudent = studentRepository.findById(savedStudent.getStudentId());
        assertTrue(foundStudent.isPresent());
        assertEquals("789012", foundStudent.get().getIndexNumber());
        assertEquals("Piotr", foundStudent.get().getUser().getFirstName());
    }

    @Test
    @DisplayName("Should update existing student")
    void shouldUpdateExistingStudent() {
        student1.setIndexNumber("111111");

        Student updatedStudent = studentRepository.save(student1);

        assertEquals(student1.getStudentId(), updatedStudent.getStudentId());
        assertEquals("111111", updatedStudent.getIndexNumber());

        Optional<Student> foundStudent = studentRepository.findById(student1.getStudentId());
        assertTrue(foundStudent.isPresent());
        assertEquals("111111", foundStudent.get().getIndexNumber());
    }

    @Test
    @DisplayName("Should delete student")
    void shouldDeleteStudent() {
        studentRepository.delete(student2);

        Optional<Student> foundStudent = studentRepository.findById(student2.getStudentId());
        assertFalse(foundStudent.isPresent());
    }
}
