package pl.edu.agh.student_registration_system.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCourseCodeAndCourseIdNot(@Size(max = 50) String courseCode, Long courseId);

    boolean existsByCourseCode(@NotBlank @Size(max = 50) String courseCode);

    Page<Course> findAll(Specification<Course> spec, Pageable pageable);
}
