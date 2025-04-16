package pl.edu.agh.student_registration_system.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateCourseDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;

public interface CourseService {
    Page<CourseResponse> searchCourses(String search, Long teacherId, Pageable pageable);

    boolean existsById(Long courseId);

    CourseResponse createCourse(@Valid CreateCourseDTO createCourseDto);

    CourseResponse getCourseResponseById(Long courseId);

    CourseResponse updateCourse(Long courseId, @Valid UpdateCourseDTO updateCourseDto);

    void deleteCourse(Long courseId);
}
