package pl.edu.agh.student_registration_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.response.EnrollmentResponse;
import pl.edu.agh.student_registration_system.service.EnrollmentService;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/my/{groupId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enrollCurrentUser(@PathVariable Long groupId) {
        EnrollmentResponse enrollment = enrollmentService.enrollCurrentUser(groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @DeleteMapping("/my/{groupId}")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Void> unenrollCurrentUser(@PathVariable Long groupId) {
        enrollmentService.unenrollCurrentUser(groupId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/admin/groups/{groupId}/students/{studentId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<EnrollmentResponse> adminEnrollStudent(@PathVariable Long groupId, @PathVariable Long studentId) {
        EnrollmentResponse enrollment = enrollmentService.enrollStudentById(studentId, groupId, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @DeleteMapping("/admin/groups/{groupId}/students/{studentId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Void> adminUnenrollStudent(@PathVariable Long groupId, @PathVariable Long studentId) {
        enrollmentService.unenrollStudentById(studentId, groupId);
        return ResponseEntity.noContent().build();
    }

}