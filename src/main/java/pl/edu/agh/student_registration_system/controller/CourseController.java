package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateCourseDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.service.CourseService;
import pl.edu.agh.student_registration_system.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<CourseResponse> addCourse(@Valid @RequestBody CreateCourseDTO createCourseDto) {
        CourseResponse newCourse = courseService.createCourse(createCourseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCourse);
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('DEANERY_STAFF', 'STUDENT', 'TEACHER')")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long courseId) {
        CourseResponse course = courseService.getCourseResponseById(courseId);
        return ResponseEntity.ok(course);
    }

    @PatchMapping("/{courseId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId, @Valid @RequestBody UpdateCourseDTO updateCourseDto) {
        CourseResponse updatedCourse = courseService.updateCourse(courseId, updateCourseDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DEANERY_STAFF', 'STUDENT', 'TEACHER')")
    public ResponseEntity<Page<CourseResponse>> searchCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long teacherId,
            Pageable pageable) {
        Page<CourseResponse> page = courseService.searchCourses(search, teacherId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{courseId}/groups")
    @PreAuthorize("hasAnyAuthority('DEANERY_STAFF', 'STUDENT', 'TEACHER')")
    public ResponseEntity<List<GroupResponse>> getGroupsForCourse(@PathVariable Long courseId) {
        if (!courseService.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        List<GroupResponse> groups = groupService.getGroupsByCourseId(courseId);
        return ResponseEntity.ok(groups);
    }

    @DeleteMapping("/{courseId}/groups")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Void> deleteAllGroupsForCourse(@PathVariable Long courseId) {
        groupService.deleteAllGroupsByCourseId(courseId);
        return ResponseEntity.noContent().build();
    }
}