package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EnrollmentModelTest {

    @Test
    void shouldCreateEnrollmentWithAllFields() {
        Student student = new Student();
        CourseGroup group = new CourseGroup();
        LocalDateTime enrollmentDate = LocalDateTime.now();

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(1L);
        enrollment.setEnrollmentDate(enrollmentDate);
        enrollment.setStudent(student);
        enrollment.setGroup(group);

        assertEquals(1L, enrollment.getEnrollmentId());
        assertEquals(enrollmentDate, enrollment.getEnrollmentDate());
        assertEquals(student, enrollment.getStudent());
        assertEquals(group, enrollment.getGroup());
    }

    @Test
    void shouldCreateEnrollmentWithConstructor() {
        Student student = new Student();
        CourseGroup group = new CourseGroup();
        LocalDateTime enrollmentDate = LocalDateTime.now();

        Enrollment enrollment = new Enrollment(1L, enrollmentDate, student, group);

        assertEquals(1L, enrollment.getEnrollmentId());
        assertEquals(enrollmentDate, enrollment.getEnrollmentDate());
        assertEquals(student, enrollment.getStudent());
        assertEquals(group, enrollment.getGroup());
    }

    @Test
    void shouldUpdateEnrollmentDate() {
        Enrollment enrollment = new Enrollment();
        LocalDateTime initialDate = LocalDateTime.of(2025, 1, 1, 10, 0);
        enrollment.setEnrollmentDate(initialDate);

        assertEquals(initialDate, enrollment.getEnrollmentDate());

        LocalDateTime newDate = LocalDateTime.of(2025, 2, 1, 10, 0);
        enrollment.setEnrollmentDate(newDate);
        assertEquals(newDate, enrollment.getEnrollmentDate());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setEnrollmentId(1L);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setEnrollmentId(1L);

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setEnrollmentId(2L);

        assertEquals(enrollment1, enrollment2);
        assertNotEquals(enrollment1, enrollment3);
        assertEquals(enrollment1.hashCode(), enrollment2.hashCode());
        assertNotEquals(enrollment1.hashCode(), enrollment3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(1L);
        LocalDateTime enrollmentDate = LocalDateTime.of(2025, 1, 1, 10, 0);
        enrollment.setEnrollmentDate(enrollmentDate);

        String toString = enrollment.toString();

        assertTrue(toString.contains("enrollmentId=1"));
        assertTrue(toString.contains("enrollmentDate=" + enrollmentDate));
    }
}
