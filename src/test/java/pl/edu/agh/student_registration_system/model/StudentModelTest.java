package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class StudentModelTest {

    @Test
    void shouldCreateStudentWithAllFields() {
        User user = new User();

        Student student = new Student();
        student.setStudentId(1L);
        student.setIndexNumber("123456");
        student.setUser(user);

        assertEquals(1L, student.getStudentId());
        assertEquals("123456", student.getIndexNumber());
        assertEquals(user, student.getUser());
        assertNotNull(student.getEnrollments());
        assertNotNull(student.getGrades());
        assertNotNull(student.getAttendanceRecords());
    }

    @Test
    void shouldCreateStudentWithConstructor() {
        User user = new User();

        Student student = new Student(1L, "123456", user, new HashSet<>(), new HashSet<>(), new HashSet<>());

        assertEquals(1L, student.getStudentId());
        assertEquals("123456", student.getIndexNumber());
        assertEquals(user, student.getUser());
        assertNotNull(student.getEnrollments());
        assertNotNull(student.getGrades());
        assertNotNull(student.getAttendanceRecords());
    }

    @Test
    void shouldAddEnrollment() {
        Student student = new Student();
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);

        student.getEnrollments().add(enrollment);

        assertEquals(1, student.getEnrollments().size());
        assertTrue(student.getEnrollments().contains(enrollment));
    }

    @Test
    void shouldAddGrade() {
        Student student = new Student();
        Grade grade = new Grade();
        grade.setStudent(student);

        student.getGrades().add(grade);

        assertEquals(1, student.getGrades().size());
        assertTrue(student.getGrades().contains(grade));
    }

    @Test
    void shouldAddAttendanceRecord() {
        Student student = new Student();
        Attendance attendance = new Attendance();
        attendance.setStudent(student);

        student.getAttendanceRecords().add(attendance);

        assertEquals(1, student.getAttendanceRecords().size());
        assertTrue(student.getAttendanceRecords().contains(attendance));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Student student1 = new Student();
        student1.setStudentId(1L);

        Student student2 = new Student();
        student2.setStudentId(1L);

        Student student3 = new Student();
        student3.setStudentId(2L);

        assertEquals(student1, student2);
        assertNotEquals(student1, student3);
        assertEquals(student1.hashCode(), student2.hashCode());
        assertNotEquals(student1.hashCode(), student3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Student student = new Student();
        student.setStudentId(1L);
        student.setIndexNumber("123456");

        String toString = student.toString();

        assertTrue(toString.contains("studentId=1"));
        assertTrue(toString.contains("indexNumber=123456"));
    }
}
