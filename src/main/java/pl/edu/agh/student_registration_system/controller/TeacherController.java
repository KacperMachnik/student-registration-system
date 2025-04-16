package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.dto.UpdateTeacherDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.TeacherResponse;
import pl.edu.agh.student_registration_system.service.TeacherService;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/{teacherId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long teacherId) {
        TeacherResponse teacher = teacherService.getTeacherResponseById(teacherId);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<TeacherResponse> getCurrentTeacherData() {
        TeacherResponse teacher = teacherService.getCurrentTeacherResponse();
        return ResponseEntity.ok(teacher);
    }

    @PatchMapping("/{teacherId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long teacherId, @Valid @RequestBody UpdateTeacherDTO updateTeacherDto) {
        TeacherResponse updatedTeacher = teacherService.updateTeacher(teacherId, updateTeacherDto);
        return ResponseEntity.ok(updatedTeacher);
    }

    @DeleteMapping("/{teacherId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long teacherId) {
        teacherService.deleteTeacherAndUser(teacherId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Page<TeacherResponse>> searchTeachers(
            @RequestParam(required = false) String search, Pageable pageable) {
        Page<TeacherResponse> page = teacherService.searchTeachers(search, pageable);
        return ResponseEntity.ok(page);
    }


    @GetMapping("/me/courses")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<List<CourseResponse>> getCurrentTeacherCourses() {
        List<CourseResponse> courses = teacherService.getCurrentTeacherCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/me/groups")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<List<GroupResponse>> getCurrentTeacherGroups(
            @RequestParam(required = false) Long courseId) {
        List<GroupResponse> groups = teacherService.getCurrentTeacherGroups(courseId);
        return ResponseEntity.ok(groups);
    }
}