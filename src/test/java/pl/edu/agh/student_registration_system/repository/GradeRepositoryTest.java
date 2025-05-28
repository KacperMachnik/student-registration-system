package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class GradeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GradeRepository gradeRepository;

    private Student student1;
    private Course course1, course2;
    private Teacher teacher1;
    private Grade grade1, grade2, grade3;


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

        User userT1 = new User(null, "Teacher", "One", "pass", "t1@example.com", true, teacherRole, null, null);
        entityManager.persist(userT1);
        teacher1 = new Teacher(null, "Dr.", userT1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher1);

        course1 = new Course(null, "Course X", "CX1", "Desc X", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course1);
        course2 = new Course(null, "Course Y", "CY1", "Desc Y", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(course2);

        grade1 = new Grade(null, "A", LocalDateTime.now(), "Good", student1, course1, teacher1);
        gradeRepository.save(grade1);
        grade2 = new Grade(null, "B", LocalDateTime.now().minusDays(1), "Ok", student1, course1, teacher1);
        gradeRepository.save(grade2);
        grade3 = new Grade(null, "C", LocalDateTime.now(), "Pass", student1, course2, teacher1);
        gradeRepository.save(grade3);

        entityManager.flush();
    }

    @Test
    void testFindByStudentAndCourseWithDetails() {
        List<Grade> gradesS1C1 = gradeRepository.findByStudentAndCourseWithDetails(student1, course1);
        assertThat(gradesS1C1).hasSize(2).containsExactlyInAnyOrder(grade1, grade2);
        gradesS1C1.forEach(g -> {
            assertThat(g.getStudent().getUser()).isNotNull();
            assertThat(g.getCourse()).isNotNull();
            assertThat(g.getTeacher().getUser()).isNotNull();
        });

        List<Grade> gradesS1C2 = gradeRepository.findByStudentAndCourseWithDetails(student1, course2);
        assertThat(gradesS1C2).hasSize(1).containsExactly(grade3);
    }

    @Test
    void testFindByStudentWithDetails() {
        List<Grade> gradesS1 = gradeRepository.findByStudentWithDetails(student1);
        assertThat(gradesS1).hasSize(3).containsExactlyInAnyOrder(grade1, grade2, grade3);
        gradesS1.forEach(g -> {
            assertThat(g.getStudent().getUser()).isNotNull();
            assertThat(g.getCourse()).isNotNull();
            assertThat(g.getTeacher().getUser()).isNotNull();
        });
    }

    @Test
    void testFindAllByStudentAndCourseWithDetails() {
        List<Grade> gradesS1C1 = gradeRepository.findAllByStudentAndCourseWithDetails(student1, course1);
        assertThat(gradesS1C1).hasSize(2).containsExactlyInAnyOrder(grade1, grade2);
        gradesS1C1.forEach(g -> {
            assertThat(g.getStudent().getUser()).isNotNull();
            assertThat(g.getCourse()).isNotNull();
            assertThat(g.getTeacher().getUser()).isNotNull();
        });
    }
}