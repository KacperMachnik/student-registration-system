package pl.edu.agh.student_registration_system.model;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;

@DataJpaTest
class CourseModelTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testPersistAndFindCourse() {
        Course course = new Course(null, "Introduction to Programming", "PROG101", "Basic programming concepts", 5, new HashSet<>(), new HashSet<>());
        Course savedCourse = entityManager.persistAndFlush(course);

        assertThat(savedCourse).isNotNull();
        assertThat(savedCourse.getCourseId()).isNotNull();
        assertThat(savedCourse.getCourseName()).isEqualTo("Introduction to Programming");
        assertThat(savedCourse.getCourseCode()).isEqualTo("PROG101");
        assertThat(savedCourse.getCredits()).isEqualTo(5);

        Optional<Course> foundCourse = Optional.ofNullable(entityManager.find(Course.class, savedCourse.getCourseId()));
        assertThat(foundCourse).isPresent();
        assertThat(foundCourse.get()).isEqualTo(savedCourse);
    }

    @Test
    void testCourseCodeUniqueConstraint() {
        Course course1 = new Course(null, "Math I", "MATH100", "Description", 4, new HashSet<>(), new HashSet<>());
        entityManager.persistAndFlush(course1);

        Course course2 = new Course(null, "Math II", "MATH100", "Another Description", 4, new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(course2);
        });
    }

    @Test
    void testCourseNameIsNotNull() {
        Course course = new Course(null, null, "CS101", "Description", 3, new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(course);
        });
    }

    @Test
    void testCourseCodeIsNotNull() {
        Course course = new Course(null, "Data Structures", null, "Description", 3, new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(course);
        });
    }

    @Test
    void testOneToManyCourseGroupsRelationship() {
        Course course = new Course(null, "Advanced Algorithms", "ALG300", "Advanced topics", 6, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);

        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        User teacherUser = new User(null, "Walter", "White", "pass", "w.white@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        Teacher teacher = new Teacher(null, "Dr.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);

        CourseGroup group1 = new CourseGroup(null, 1, 20, course, teacher, new HashSet<>(), new ArrayList<>());
        CourseGroup group2 = new CourseGroup(null, 2, 25, course, teacher, new HashSet<>(), new ArrayList<>());

        course.getCourseGroups().add(group1);
        course.getCourseGroups().add(group2);

        entityManager.persistAndFlush(group1);
        entityManager.persistAndFlush(group2);
        entityManager.clear();

        Course foundCourse = entityManager.find(Course.class, course.getCourseId());
        assertThat(foundCourse.getCourseGroups()).hasSize(2);
        assertThat(foundCourse.getCourseGroups()).contains(group1, group2);

        foundCourse.getCourseGroups().removeIf(g -> g.getGroupNumber() == 1);
        entityManager.flush();
        entityManager.clear();
        foundCourse = entityManager.find(Course.class, course.getCourseId());
        assertThat(foundCourse.getCourseGroups()).hasSize(1);
    }
}