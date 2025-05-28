package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CourseGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseGroupRepository courseGroupRepository;

    private Teacher teacher1, teacher2;
    private Student student1, student2;
    private Course course1, course2;
    private CourseGroup group1, group2, group3;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        User userT1 = new User(null, "Teacher", "One", "pass", "t1@example.com", true, teacherRole, null, null);
        entityManager.persist(userT1);
        teacher1 = new Teacher(null, "Dr.", userT1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher1);

        User userT2 = new User(null, "Teacher", "Two", "pass", "t2@example.com", true, teacherRole, null, null);
        entityManager.persist(userT2);
        teacher2 = new Teacher(null, "Prof.", userT2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher2);

        User userS1 = new User(null, "Student", "One", "pass", "s1@example.com", true, studentRole, null, null);
        entityManager.persist(userS1);
        student1 = new Student(null, "100001", userS1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student1);

        User userS2 = new User(null, "Student", "Two", "pass", "s2@example.com", true, studentRole, null, null);
        entityManager.persist(userS2);
        student2 = new Student(null, "100002", userS2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student2);

        course1 = new Course(null, "Course Alpha", "CA101", "Desc Alpha", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(course1);
        course2 = new Course(null, "Course Beta", "CB202", "Desc Beta", 5, new HashSet<>(), new HashSet<>());
        entityManager.persist(course2);

        group1 = new CourseGroup(null, 1, 20, course1, teacher1, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group1);
        group2 = new CourseGroup(null, 2, 25, course1, teacher1, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group2);
        group3 = new CourseGroup(null, 1, 30, course2, teacher2, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group3);

        Enrollment enrollment1 = new Enrollment(null, LocalDateTime.now(), student1, group1);
        entityManager.persist(enrollment1);
        Enrollment enrollment2 = new Enrollment(null, LocalDateTime.now(), student2, group1);
        entityManager.persist(enrollment2);
        Enrollment enrollment3 = new Enrollment(null, LocalDateTime.now(), student1, group3);
        entityManager.persist(enrollment3);

        entityManager.flush();
    }

    @Test
    void testExistsByTeacherAndEnrollmentsStudent() {
        boolean exists = courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher1, student1);
        assertThat(exists).isTrue();

        boolean notExists = courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher2, student2);
        assertThat(notExists).isFalse();

        boolean notExistsForTeacher1Student2 = courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher1, student2);
        assertThat(notExistsForTeacher1Student2).isTrue();
    }

    @Test
    void testCountByTeacher() {
        Long countT1 = courseGroupRepository.countByTeacher(teacher1);
        assertThat(countT1).isEqualTo(2);

        Long countT2 = courseGroupRepository.countByTeacher(teacher2);
        assertThat(countT2).isEqualTo(1);
    }

    @Test
    void testFindByTeacherWithCourse() {
        List<CourseGroup> groupsForT1 = courseGroupRepository.findByTeacherWithCourse(teacher1);
        assertThat(groupsForT1).hasSize(2);
        assertThat(groupsForT1).extracting(CourseGroup::getCourse).containsOnly(course1);
    }

    @Test
    void testFindByTeacherAndCourseWithDetails() {
        List<CourseGroup> groups = courseGroupRepository.findByTeacherAndCourseWithDetails(teacher1, course1);
        assertThat(groups).hasSize(2);
        assertThat(groups).contains(group1, group2);
        groups.forEach(g -> {
            assertThat(g.getCourse()).isNotNull();
            assertThat(g.getTeacher()).isNotNull();
            assertThat(g.getTeacher().getUser()).isNotNull();
        });
    }

    @Test
    void testFindByTeacherWithDetails() {
        List<CourseGroup> groupsForT1 = courseGroupRepository.findByTeacherWithDetails(teacher1);
        assertThat(groupsForT1).hasSize(2);
        groupsForT1.forEach(g -> {
            assertThat(g.getCourse()).isNotNull();
            assertThat(g.getTeacher()).isNotNull();
            assertThat(g.getTeacher().getUser()).isNotNull();
        });
    }

    @Test
    void testFindByCourseWithDetails() {
        List<CourseGroup> groupsForC1 = courseGroupRepository.findByCourseWithDetails(course1);
        assertThat(groupsForC1).hasSize(2);
        groupsForC1.forEach(g -> {
            assertThat(g.getCourse()).isNotNull();
            if (g.getTeacher() != null) {
                assertThat(g.getTeacher().getUser()).isNotNull();
            }
        });
    }

    @Test
    void testFindByIdWithDetails() {
        Optional<CourseGroup> foundGroup1 = courseGroupRepository.findByIdWithDetails(group1.getCourseGroupId());
        assertThat(foundGroup1).isPresent();
        assertThat(foundGroup1.get().getCourse()).isNotNull();
        assertThat(foundGroup1.get().getTeacher()).isNotNull();
        assertThat(foundGroup1.get().getTeacher().getUser()).isNotNull();
    }

    @Test
    void testFindByCourse() {
        List<CourseGroup> groupsForC1 = courseGroupRepository.findByCourse(course1);
        assertThat(groupsForC1).hasSize(2).contains(group1, group2);

        List<CourseGroup> groupsForC2 = courseGroupRepository.findByCourse(course2);
        assertThat(groupsForC2).hasSize(1).contains(group3);
    }

    @Test
    void testExistsByCourseAndGroupNumberAndCourseGroupIdNot() {
        boolean exists = courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course1, 1, group2.getCourseGroupId());
        assertThat(exists).isTrue();

        boolean notExists = courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course1, 3, group1.getCourseGroupId());
        assertThat(notExists).isFalse();

        boolean existsSameId = courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course1, 1, group1.getCourseGroupId());
        assertThat(existsSameId).isFalse();
    }

    @Test
    void testExistsByCourseAndGroupNumber() {
        boolean exists = courseGroupRepository.existsByCourseAndGroupNumber(course1, 1);
        assertThat(exists).isTrue();

        boolean notExists = courseGroupRepository.existsByCourseAndGroupNumber(course1, 3);
        assertThat(notExists).isFalse();
    }
}