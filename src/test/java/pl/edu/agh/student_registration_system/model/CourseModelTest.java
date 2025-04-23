package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class CourseModelTest {

    @Test
    void shouldCreateCourseWithAllFields() {
        Course course = new Course();
        course.setCourseId(1L);
        course.setCourseName("Java Programming");
        course.setCourseCode("JP101");
        course.setDescription("Introduction to Java Programming");
        course.setCredits(5);

        assertEquals(1L, course.getCourseId());
        assertEquals("Java Programming", course.getCourseName());
        assertEquals("JP101", course.getCourseCode());
        assertEquals("Introduction to Java Programming", course.getDescription());
        assertEquals(5, course.getCredits());
        assertNotNull(course.getCourseGroups());
        assertNotNull(course.getGrades());
    }

    @Test
    void shouldCreateCourseWithConstructor() {
        Course course = new Course(1L, "Java Programming", "JP101",
                "Introduction to Java Programming", 5, new HashSet<>(), new HashSet<>());

        assertEquals(1L, course.getCourseId());
        assertEquals("Java Programming", course.getCourseName());
        assertEquals("JP101", course.getCourseCode());
        assertEquals("Introduction to Java Programming", course.getDescription());
        assertEquals(5, course.getCredits());
        assertNotNull(course.getCourseGroups());
        assertNotNull(course.getGrades());
    }

    @Test
    void shouldAddCourseGroup() {
        Course course = new Course();
        CourseGroup group = new CourseGroup();
        group.setCourse(course);

        course.getCourseGroups().add(group);

        assertEquals(1, course.getCourseGroups().size());
        assertTrue(course.getCourseGroups().contains(group));
    }

    @Test
    void shouldAddGrade() {
        Course course = new Course();
        Grade grade = new Grade();
        grade.setCourse(course);

        course.getGrades().add(grade);

        assertEquals(1, course.getGrades().size());
        assertTrue(course.getGrades().contains(grade));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Course course1 = new Course();
        course1.setCourseId(1L);

        Course course2 = new Course();
        course2.setCourseId(1L);

        Course course3 = new Course();
        course3.setCourseId(2L);

        assertEquals(course1, course2);
        assertNotEquals(course1, course3);
        assertEquals(course1.hashCode(), course2.hashCode());
        assertNotEquals(course1.hashCode(), course3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Course course = new Course();
        course.setCourseId(1L);
        course.setCourseName("Java Programming");
        course.setCourseCode("JP101");

        String toString = course.toString();

        assertTrue(toString.contains("courseId=1"));
        assertTrue(toString.contains("courseName=Java Programming"));
        assertTrue(toString.contains("courseCode=JP101"));
    }
}
