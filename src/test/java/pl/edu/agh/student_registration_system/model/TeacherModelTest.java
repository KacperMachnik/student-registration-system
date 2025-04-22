package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class TeacherModelTest {

    @Test
    void shouldCreateTeacherWithAllFields() {
        User user = new User();

        Teacher teacher = new Teacher();
        teacher.setTeacherId(1L);
        teacher.setTitle("Prof.");
        teacher.setUser(user);

        assertEquals(1L, teacher.getTeacherId());
        assertEquals("Prof.", teacher.getTitle());
        assertEquals(user, teacher.getUser());
        assertNotNull(teacher.getTaughtGroups());
        assertNotNull(teacher.getIssuedGrades());
        assertNotNull(teacher.getRecordedAttendances());
    }

    @Test
    void shouldCreateTeacherWithConstructor() {
        User user = new User();

        Teacher teacher = new Teacher(1L, "Dr", user, new HashSet<>(), new HashSet<>(), new HashSet<>());

        assertEquals(1L, teacher.getTeacherId());
        assertEquals("Dr", teacher.getTitle());
        assertEquals(user, teacher.getUser());
        assertNotNull(teacher.getTaughtGroups());
        assertNotNull(teacher.getIssuedGrades());
        assertNotNull(teacher.getRecordedAttendances());
    }

    @Test
    void shouldAddTaughtGroup() {
        Teacher teacher = new Teacher();
        CourseGroup group = new CourseGroup();
        group.setTeacher(teacher);

        teacher.getTaughtGroups().add(group);

        assertEquals(1, teacher.getTaughtGroups().size());
        assertTrue(teacher.getTaughtGroups().contains(group));
    }

    @Test
    void shouldAddIssuedGrade() {
        Teacher teacher = new Teacher();
        Grade grade = new Grade();
        grade.setTeacher(teacher);

        teacher.getIssuedGrades().add(grade);

        assertEquals(1, teacher.getIssuedGrades().size());
        assertTrue(teacher.getIssuedGrades().contains(grade));
    }

    @Test
    void shouldAddRecordedAttendance() {
        Teacher teacher = new Teacher();
        Attendance attendance = new Attendance();
        attendance.setRecordedByTeacher(teacher);

        teacher.getRecordedAttendances().add(attendance);

        assertEquals(1, teacher.getRecordedAttendances().size());
        assertTrue(teacher.getRecordedAttendances().contains(attendance));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Teacher teacher1 = new Teacher();
        teacher1.setTeacherId(1L);

        Teacher teacher2 = new Teacher();
        teacher2.setTeacherId(1L);

        Teacher teacher3 = new Teacher();
        teacher3.setTeacherId(2L);

        assertEquals(teacher1, teacher2);
        assertNotEquals(teacher1, teacher3);
        assertEquals(teacher1.hashCode(), teacher2.hashCode());
        assertNotEquals(teacher1.hashCode(), teacher3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(1L);
        teacher.setTitle("Prof.");

        String toString = teacher.toString();

        assertTrue(toString.contains("teacherId=1"));
        assertTrue(toString.contains("title=Prof."));
    }
}
