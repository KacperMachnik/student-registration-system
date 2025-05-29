package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Student student1, student2;
    private Course course1, course2;
    private CourseGroup group1, group2;
    private Teacher teacher1;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        User userS1 = new User(null, "Student", "One", "pass", "s1@example.com", true, studentRole, null, null);
        entityManager.persist(userS1);
        student1 = new Student(null, "100001", userS1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student1);

        User userS2 = new User(null, "Student", "Two", "pass", "s2@example.com", true, studentRole, null, null);
        entityManager.persist(userS2);
        student2 = new Student(null, "100002", userS2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student2);

        User userT1 = new User(null, "Teacher", "One", "pass", "t1@example.com", true, teacherRole, null, null);
        entityManager.persist(userT1);
        teacher1 = new Teacher(null, "Dr.", userT1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher1);

        course1 = new Course(null, "Course A", "CA1", "Desc A", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course1);
        course2 = new Course(null, "Course B", "CB1", "Desc B", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(course2);

        group1 = new CourseGroup(null, 1, 10, course1, teacher1, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group1);
        group2 = new CourseGroup(null, 1, 15, course2, teacher1, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group2);

        Enrollment enrollment1 = new Enrollment(null, LocalDateTime.now(), student1, group1);
        enrollmentRepository.save(enrollment1);
        Enrollment enrollment2 = new Enrollment(null, LocalDateTime.now(), student2, group1);
        enrollmentRepository.save(enrollment2);
        Enrollment enrollment3 = new Enrollment(null, LocalDateTime.now(), student1, group2);
        enrollmentRepository.save(enrollment3);

        entityManager.flush();
    }

    @Test
    void testFindByStudent() {
        List<Enrollment> enrollmentsS1 = enrollmentRepository.findByStudent(student1);
        assertThat(enrollmentsS1).hasSize(2);
        assertThat(enrollmentsS1).extracting(Enrollment::getGroup).containsExactlyInAnyOrder(group1, group2);
    }

    @Test
    void testCountByGroup() {
        Integer countGroup1 = enrollmentRepository.countByGroup(group1);
        assertThat(countGroup1).isEqualTo(2);

        Integer countGroup2 = enrollmentRepository.countByGroup(group2);
        assertThat(countGroup2).isEqualTo(1);
    }

    @Test
    void testFindByStudentWithGroupAndCourse() {
        List<Enrollment> enrollmentsS1 = enrollmentRepository.findByStudentWithGroupAndCourse(student1);
        assertThat(enrollmentsS1).hasSize(2);
        enrollmentsS1.forEach(e -> {
            assertThat(e.getGroup()).isNotNull();
            assertThat(e.getGroup().getCourse()).isNotNull();
            if (e.getGroup().getTeacher() != null) {
                assertThat(e.getGroup().getTeacher().getUser()).isNotNull();
            }
        });
    }

    @Test
    void testFindStudentsByGroupId() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Student> studentsInGroup1 = enrollmentRepository.findStudentsByGroupId(group1.getCourseGroupId(), pageable);
        assertThat(studentsInGroup1.getTotalElements()).isEqualTo(2);
        assertThat(studentsInGroup1.getContent()).containsExactlyInAnyOrder(student1, student2);
        studentsInGroup1.getContent().forEach(s -> {
            assertThat(s.getUser()).isNotNull();
            assertThat(s.getUser().getRole()).isNotNull();
        });
    }

    @Test
    void testFindCourseIdsByStudent() {
        List<Long> courseIdsS1 = enrollmentRepository.findCourseIdsByStudent(student1);
        assertThat(courseIdsS1).hasSize(2).containsExactlyInAnyOrder(course1.getCourseId(), course2.getCourseId());
    }

    @Test
    void testExistsByStudentAndGroup_Course() {
        boolean existsS1C1 = enrollmentRepository.existsByStudentAndGroup_Course(student1, course1);
        assertThat(existsS1C1).isTrue();

        boolean existsS2C2 = enrollmentRepository.existsByStudentAndGroup_Course(student2, course2);
        assertThat(existsS2C2).isFalse();
    }

    @Test
    void testFindByStudentAndGroup_CourseGroupId() {
        Optional<Enrollment> enrollmentS1G1 = enrollmentRepository.findByStudentAndGroup_CourseGroupId(student1, group1.getCourseGroupId());
        assertThat(enrollmentS1G1).isPresent();
        assertThat(enrollmentS1G1.get().getStudent()).isEqualTo(student1);
        assertThat(enrollmentS1G1.get().getGroup()).isEqualTo(group1);

        Optional<Enrollment> enrollmentS2G2 = enrollmentRepository.findByStudentAndGroup_CourseGroupId(student2, group2.getCourseGroupId());
        assertThat(enrollmentS2G2).isNotPresent();
    }

    @Test
    void testFindByGroup() {
        List<Enrollment> enrollmentsInGroup1 = enrollmentRepository.findByGroup(group1);
        assertThat(enrollmentsInGroup1).hasSize(2);
        assertThat(enrollmentsInGroup1).extracting(Enrollment::getStudent).containsExactlyInAnyOrder(student1, student2);
    }

    @Test
    void testExistsByStudentAndGroup_CourseGroupId() {
        boolean existsS1G1 = enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student1, group1.getCourseGroupId());
        assertThat(existsS1G1).isTrue();

        boolean notExistsS2G2 = enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student2, group2.getCourseGroupId());
        assertThat(notExistsS2G2).isFalse();
    }
}