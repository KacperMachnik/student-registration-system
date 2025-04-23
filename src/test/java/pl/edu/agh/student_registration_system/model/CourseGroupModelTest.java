package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class CourseGroupModelTest {

    @Test
    void shouldCreateCourseGroupWithAllFields() {
        Course course = new Course();
        Teacher teacher = new Teacher();

        CourseGroup group = new CourseGroup();
        group.setCourseGroupId(1L);
        group.setGroupNumber(101);
        group.setMaxCapacity(30);
        group.setCourse(course);
        group.setTeacher(teacher);

        assertEquals(1L, group.getCourseGroupId());
        assertEquals(101, group.getGroupNumber());
        assertEquals(30, group.getMaxCapacity());
        assertEquals(course, group.getCourse());
        assertEquals(teacher, group.getTeacher());
        assertNotNull(group.getEnrollments());
        assertNotNull(group.getMeetings());
    }

    @Test
    void shouldCreateCourseGroupWithConstructor() {
        Course course = new Course();
        Teacher teacher = new Teacher();

        CourseGroup group = new CourseGroup(1L, 101, 30, course, teacher, new HashSet<>(), new ArrayList<>());

        assertEquals(1L, group.getCourseGroupId());
        assertEquals(101, group.getGroupNumber());
        assertEquals(30, group.getMaxCapacity());
        assertEquals(course, group.getCourse());
        assertEquals(teacher, group.getTeacher());
        assertNotNull(group.getEnrollments());
        assertNotNull(group.getMeetings());
    }

    @Test
    void shouldAddEnrollment() {
        CourseGroup group = new CourseGroup();
        Enrollment enrollment = new Enrollment();
        enrollment.setGroup(group);

        group.getEnrollments().add(enrollment);

        assertEquals(1, group.getEnrollments().size());
        assertTrue(group.getEnrollments().contains(enrollment));
    }

    @Test
    void shouldAddMeeting() {
        CourseGroup group = new CourseGroup();
        Meeting meeting = new Meeting();
        meeting.setGroup(group);

        group.getMeetings().add(meeting);

        assertEquals(1, group.getMeetings().size());
        assertTrue(group.getMeetings().contains(meeting));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        CourseGroup group1 = new CourseGroup();
        group1.setCourseGroupId(1L);

        CourseGroup group2 = new CourseGroup();
        group2.setCourseGroupId(1L);

        CourseGroup group3 = new CourseGroup();
        group3.setCourseGroupId(2L);

        assertEquals(group1, group2);
        assertNotEquals(group1, group3);
        assertEquals(group1.hashCode(), group2.hashCode());
        assertNotEquals(group1.hashCode(), group3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        CourseGroup group = new CourseGroup();
        group.setCourseGroupId(1L);
        group.setGroupNumber(101);
        group.setMaxCapacity(30);

        String toString = group.toString();

        assertTrue(toString.contains("courseGroupId=1"));
        assertTrue(toString.contains("groupNumber=101"));
        assertTrue(toString.contains("maxCapacity=30"));
    }
}
