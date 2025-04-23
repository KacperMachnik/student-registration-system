package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class GradeModelTest {

    @Test
    void shouldCreateGradeWithAllFields() {
        Student student = new Student();
        Course course = new Course();
        Teacher teacher = new Teacher();
        LocalDateTime gradeDate = LocalDateTime.now();

        Grade grade = new Grade();
        grade.setGradeId(1L);
        grade.setGradeValue("5.0");
        grade.setGradeDate(gradeDate);
        grade.setComment("Excellent work");
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setTeacher(teacher);

        assertEquals(1L, grade.getGradeId());
        assertEquals("5.0", grade.getGradeValue());
        assertEquals(gradeDate, grade.getGradeDate());
        assertEquals("Excellent work", grade.getComment());
        assertEquals(student, grade.getStudent());
        assertEquals(course, grade.getCourse());
        assertEquals(teacher, grade.getTeacher());
    }

    @Test
    void shouldCreateGradeWithConstructor() {
        Student student = new Student();
        Course course = new Course();
        Teacher teacher = new Teacher();
        LocalDateTime gradeDate = LocalDateTime.now();

        Grade grade = new Grade(1L, "4.5", gradeDate, "Good job", student, course, teacher);

        assertEquals(1L, grade.getGradeId());
        assertEquals("4.5", grade.getGradeValue());
        assertEquals(gradeDate, grade.getGradeDate());
        assertEquals("Good job", grade.getComment());
        assertEquals(student, grade.getStudent());
        assertEquals(course, grade.getCourse());
        assertEquals(teacher, grade.getTeacher());
    }

    @Test
    void shouldUpdateGradeValue() {
        Grade grade = new Grade();
        grade.setGradeValue("4.0");

        assertEquals("4.0", grade.getGradeValue());

        grade.setGradeValue("5.0");
        assertEquals("5.0", grade.getGradeValue());
    }

    @Test
    void shouldUpdateComment() {
        Grade grade = new Grade();
        grade.setComment("Initial comment");

        assertEquals("Initial comment", grade.getComment());

        grade.setComment("Updated comment");
        assertEquals("Updated comment", grade.getComment());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Grade grade1 = new Grade();
        grade1.setGradeId(1L);

        Grade grade2 = new Grade();
        grade2.setGradeId(1L);

        Grade grade3 = new Grade();
        grade3.setGradeId(2L);

        assertEquals(grade1, grade2);
        assertNotEquals(grade1, grade3);
        assertEquals(grade1.hashCode(), grade2.hashCode());
        assertNotEquals(grade1.hashCode(), grade3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Grade grade = new Grade();
        grade.setGradeId(1L);
        grade.setGradeValue("5.0");

        String toString = grade.toString();

        assertTrue(toString.contains("gradeId=1"));
        assertTrue(toString.contains("gradeValue=5.0"));
    }
}
