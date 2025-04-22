package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        course1 = new Course();
        course1.setCourseName("Programowanie");
        course1.setCourseCode("PRG101");
        course1.setDescription("Kurs programowania");
        course1.setCredits(5);
        courseRepository.save(course1);

        course2 = new Course();
        course2.setCourseName("Matematyka");
        course2.setCourseCode("MAT101");
        course2.setDescription("Kurs matematyki");
        course2.setCredits(6);
        courseRepository.save(course2);

        course3 = new Course();
        course3.setCourseName("Fizyka");
        course3.setCourseCode("FIZ101");
        course3.setDescription("Kurs fizyki");
        course3.setCredits(4);
        courseRepository.save(course3);
    }

    @Test
    @DisplayName("Should check if course exists by course code excluding specific course")
    void shouldCheckIfExistsByCourseCodeAndCourseIdNot() {
        boolean exists = courseRepository.existsByCourseCodeAndCourseIdNot("MAT101", course1.getCourseId());
        boolean notExists = courseRepository.existsByCourseCodeAndCourseIdNot("XYZ999", course1.getCourseId());

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should check if course exists by course code")
    void shouldCheckIfExistsByCourseCode() {
        boolean exists = courseRepository.existsByCourseCode("PRG101");
        boolean notExists = courseRepository.existsByCourseCode("XYZ999");

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should find all courses with specification and pageable")
    void shouldFindAllWithSpecificationAndPageable() {
        Specification<Course> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("credits"), 4);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        assertEquals(2, coursePage.getTotalElements());
        assertTrue(coursePage.getContent().contains(course1));
        assertTrue(coursePage.getContent().contains(course2));
        assertFalse(coursePage.getContent().contains(course3));
    }

    @Test
    @DisplayName("Should find all courses with pageable")
    void shouldFindAllWithPageable() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<Course> coursePage = courseRepository.findAll(pageable);

        assertEquals(3, coursePage.getTotalElements());
        assertEquals(2, coursePage.getContent().size());
        assertEquals(2, coursePage.getTotalPages());
    }

    @Test
    @DisplayName("Should save new course")
    void shouldSaveNewCourse() {
        Course newCourse = new Course();
        newCourse.setCourseName("Chemia");
        newCourse.setCourseCode("CHM101");
        newCourse.setDescription("Kurs chemii");
        newCourse.setCredits(3);

        Course savedCourse = courseRepository.save(newCourse);

        assertNotNull(savedCourse.getCourseId());
        assertEquals("CHM101", savedCourse.getCourseCode());

        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getCourseId());
        assertTrue(foundCourse.isPresent());
        assertEquals("Chemia", foundCourse.get().getCourseName());
    }

    @Test
    @DisplayName("Should update existing course")
    void shouldUpdateExistingCourse() {
        course1.setCourseName("Programowanie zaawansowane");
        course1.setCredits(7);

        Course updatedCourse = courseRepository.save(course1);

        assertEquals(course1.getCourseId(), updatedCourse.getCourseId());
        assertEquals("Programowanie zaawansowane", updatedCourse.getCourseName());
        assertEquals(7, updatedCourse.getCredits());

        Optional<Course> foundCourse = courseRepository.findById(course1.getCourseId());
        assertTrue(foundCourse.isPresent());
        assertEquals("Programowanie zaawansowane", foundCourse.get().getCourseName());
        assertEquals(7, foundCourse.get().getCredits());
    }

    @Test
    @DisplayName("Should delete course")
    void shouldDeleteCourse() {
        courseRepository.delete(course3);

        Optional<Course> foundCourse = courseRepository.findById(course3.getCourseId());
        assertFalse(foundCourse.isPresent());
    }
}
