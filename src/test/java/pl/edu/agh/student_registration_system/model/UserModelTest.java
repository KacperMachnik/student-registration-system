package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    void shouldCreateUserWithAllFields() {
        Role role = new Role(RoleType.STUDENT);

        User user = new User();
        user.setUserId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("password");
        user.setEmail("jan.kowalski@example.com");
        user.setIsActive(true);
        user.setRole(role);

        assertEquals(1L, user.getUserId());
        assertEquals("Jan", user.getFirstName());
        assertEquals("Kowalski", user.getLastName());
        assertEquals("password", user.getPassword());
        assertEquals("jan.kowalski@example.com", user.getEmail());
        assertTrue(user.getIsActive());
        assertEquals(role, user.getRole());
        assertNull(user.getStudentProfile());
        assertNull(user.getTeacherProfile());
    }

    @Test
    void shouldCreateUserWithConstructor() {
        Role role = new Role(RoleType.TEACHER);
        Student studentProfile = new Student();
        Teacher teacherProfile = new Teacher();

        User user = new User(1L, "Anna", "Nowak", "password", "anna.nowak@example.com",
                true, role, studentProfile, teacherProfile);

        assertEquals(1L, user.getUserId());
        assertEquals("Anna", user.getFirstName());
        assertEquals("Nowak", user.getLastName());
        assertEquals("password", user.getPassword());
        assertEquals("anna.nowak@example.com", user.getEmail());
        assertTrue(user.getIsActive());
        assertEquals(role, user.getRole());
        assertEquals(studentProfile, user.getStudentProfile());
        assertEquals(teacherProfile, user.getTeacherProfile());
    }

    @Test
    void shouldSetStudentProfile() {
        User user = new User();
        Student student = new Student();
        student.setUser(user);

        user.setStudentProfile(student);

        assertEquals(student, user.getStudentProfile());
    }

    @Test
    void shouldSetTeacherProfile() {
        User user = new User();
        Teacher teacher = new Teacher();
        teacher.setUser(user);

        user.setTeacherProfile(teacher);

        assertEquals(teacher, user.getTeacherProfile());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        User user1 = new User();
        user1.setUserId(1L);

        User user2 = new User();
        user2.setUserId(1L);

        User user3 = new User();
        user3.setUserId(2L);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        User user = new User();
        user.setUserId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan.kowalski@example.com");

        String toString = user.toString();

        assertTrue(toString.contains("userId=1"));
        assertTrue(toString.contains("firstName=Jan"));
        assertTrue(toString.contains("lastName=Kowalski"));
        assertTrue(toString.contains("email=jan.kowalski@example.com"));
    }
}
