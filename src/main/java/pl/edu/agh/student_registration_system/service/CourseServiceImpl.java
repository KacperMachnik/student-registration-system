package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.DeletionBlockedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateCourseDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.repository.CourseRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> searchCourses(String search, Long teacherId, Pageable pageable) {
        log.debug("Searching for courses with query: '{}', teacherId filter: {}, pageable: {}", search, teacherId, pageable);

        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("courseCode")), pattern);
                Predicate nameMatch = cb.like(cb.lower(root.get("courseName")), pattern);
                predicates.add(cb.or(codeMatch, nameMatch));
            }

            if (teacherId != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<CourseGroup> subRoot = subquery.from(CourseGroup.class);
                Join<CourseGroup, Teacher> teacherJoin = subRoot.join("teacher");

                subquery.select(subRoot.get("courseGroupId"))
                        .where(
                                cb.equal(subRoot.get("course"), root),
                                cb.equal(teacherJoin.get("teacherId"), teacherId)
                        );
                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        log.info("Found {} courses matching search criteria.", coursePage.getTotalElements());
        return coursePage.map(this::mapToCourseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long courseId) {
        log.debug("Checking existence of course with ID: {}", courseId);
        return courseRepository.existsById(courseId);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseDTO createCourseDto) {
        log.info("Attempting to create a new course with code: {}", createCourseDto.getCourseCode());

        if (courseRepository.existsByCourseCode(createCourseDto.getCourseCode())) {
            log.warn("Course creation failed: Course code '{}' already exists.", createCourseDto.getCourseCode());
            throw new DataIntegrityViolationException("Course with code '" + createCourseDto.getCourseCode() + "' already exists.");
        }

        Course course = new Course();
        course.setCourseCode(createCourseDto.getCourseCode());
        course.setCourseName(createCourseDto.getCourseName());
        course.setDescription(createCourseDto.getDescription());
        course.setCredits(createCourseDto.getCredits());

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getCourseId());

        return mapToCourseResponse(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseResponseById(Long courseId) {
        log.debug("Fetching course with ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.warn("Course not found with ID: {}", courseId);
                    return new ResourceNotFoundException("Course", "id", courseId);
                });
        return mapToCourseResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, UpdateCourseDTO updateCourseDto) {
        log.info("Attempting to update course with ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.warn("Course not found for update with ID: {}", courseId);
                    return new ResourceNotFoundException("Course", "id", courseId);
                });

        boolean changed = false;

        if (updateCourseDto.getCourseCode() != null && !updateCourseDto.getCourseCode().isBlank() && !updateCourseDto.getCourseCode().equals(course.getCourseCode())) {
            if (courseRepository.existsByCourseCodeAndCourseIdNot(updateCourseDto.getCourseCode(), courseId)) {
                log.warn("Course update failed: New course code '{}' already exists for another course.", updateCourseDto.getCourseCode());
                throw new DataIntegrityViolationException("Course code '" + updateCourseDto.getCourseCode() + "' already exists.");
            }
            log.debug("Updating course code for course ID: {}", courseId);
            course.setCourseCode(updateCourseDto.getCourseCode());
            changed = true;
        }
        if (updateCourseDto.getCourseName() != null && !updateCourseDto.getCourseName().isBlank() && !updateCourseDto.getCourseName().equals(course.getCourseName())) {
            log.debug("Updating course name for course ID: {}", courseId);
            course.setCourseName(updateCourseDto.getCourseName());
            changed = true;
        }
        if (updateCourseDto.getDescription() != null && !updateCourseDto.getDescription().equals(course.getDescription())) {
            log.debug("Updating description for course ID: {}", courseId);
            course.setDescription(updateCourseDto.getDescription());
            changed = true;
        }
        if (updateCourseDto.getCredits() != null && !updateCourseDto.getCredits().equals(course.getCredits())) {
            log.debug("Updating credits for course ID: {}", courseId);
            course.setCredits(updateCourseDto.getCredits());
            changed = true;
        }

        if (changed) {
            Course updatedCourse = courseRepository.save(course);
            log.info("Course with ID: {} updated successfully.", updatedCourse.getCourseId());
            return mapToCourseResponse(updatedCourse);
        } else {
            log.info("No changes detected for course with ID: {}", courseId);
            return mapToCourseResponse(course);
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        log.info("Attempting to delete course with ID: {}", courseId);

        Course courseToDelete = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.warn("Course deletion failed: Course not found with ID: {}", courseId);
                    return new ResourceNotFoundException("Course", "id", courseId);
                });

        log.warn("Deleting course {}. This will trigger cascading deletes based on entity mappings (CascadeType.REMOVE/ALL in Course for groups/grades, and potentially further down).", courseId);
        try {
            courseRepository.delete(courseToDelete);
            log.info("Course with ID: {} and potentially related entities deleted successfully via cascade.", courseId);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete course {} due to data integrity constraints. This likely means related entities (e.g., enrollments, attendance without cascade) still exist or a database constraint prevents deletion. Original exception: {}", courseId, e.getMessage());
            throw new DeletionBlockedException(
                    "Cannot delete course: It has related data (like enrollments or attendance) that could not be automatically removed due to database constraints or incomplete cascade settings. Please remove related data manually or check cascade configurations." +
                            e.getMessage()
            );
        } catch (Exception e) {
            log.error("An unexpected error occurred while deleting course {}: {}", courseId, e.getMessage(), e);
            throw e;
        }
    }

    private CourseResponse mapToCourseResponse(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseResponse(
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getDescription(),
                course.getCredits()
        );
    }
}