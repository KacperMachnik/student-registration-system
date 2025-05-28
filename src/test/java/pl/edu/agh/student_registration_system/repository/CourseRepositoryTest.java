package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.model.Course;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    private Course course1, course2;

    @BeforeEach
    void setUp() {
        course1 = new Course(null, "Intro to CS", "CS101", "Basics", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course1);
        course2 = new Course(null, "Advanced Algo", "ALGO202", "Complex", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(course2);
        entityManager.flush();
    }

    @Test
    void testExistsByCourseCodeAndCourseIdNot() {
        boolean exists = courseRepository.existsByCourseCodeAndCourseIdNot("CS101", course2.getCourseId());
        assertThat(exists).isTrue();

        boolean notExists = courseRepository.existsByCourseCodeAndCourseIdNot("CS101", course1.getCourseId());
        assertThat(notExists).isFalse();

        boolean notExistsOtherCode = courseRepository.existsByCourseCodeAndCourseIdNot("CS999", course1.getCourseId());
        assertThat(notExistsOtherCode).isFalse();
    }

    @Test
    void testExistsByCourseCode() {
        boolean exists = courseRepository.existsByCourseCode("CS101");
        assertThat(exists).isTrue();

        boolean notExists = courseRepository.existsByCourseCode("CS999");
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindAllWithSpecificationAndPageable() {
        Course course3 = new Course(null, "Data Structures", "DS102", "Structures", 3, new HashSet<>(), new HashSet<>());
        entityManager.persistAndFlush(course3);

        Specification<Course> specCredits3 = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("credits"), 3);

        Pageable pageable = PageRequest.of(0, 2);
        Page<Course> pageOfCourses = courseRepository.findAll(specCredits3, pageable);

        assertThat(pageOfCourses.getTotalElements()).isEqualTo(2);
        assertThat(pageOfCourses.getContent()).hasSize(2);
        assertThat(pageOfCourses.getContent()).extracting(Course::getCourseCode).containsExactlyInAnyOrder("CS101", "DS102");

        Specification<Course> specNameContainsIntro = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("courseName")), "%intro%");
        Page<Course> pageIntroCourses = courseRepository.findAll(specNameContainsIntro, PageRequest.of(0, 10));
        assertThat(pageIntroCourses.getTotalElements()).isEqualTo(1);
        assertThat(pageIntroCourses.getContent().get(0).getCourseCode()).isEqualTo("CS101");
    }
}