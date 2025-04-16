package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.dto.UpdateStudentDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.StudentResponse;
import pl.edu.agh.student_registration_system.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF') or (@studentSecurityService.isTeacherAllowedToViewStudent(#studentId) and hasAuthority('TEACHER'))")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long studentId) {
        StudentResponse student = studentService.getStudentResponseById(studentId);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<StudentResponse> getCurrentStudentData() {
        StudentResponse student = studentService.getCurrentStudentResponse();
        return ResponseEntity.ok(student);
    }


    @PatchMapping("/{studentId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long studentId,
                                                         @Valid @RequestBody UpdateStudentDTO updateStudentDto) {
        StudentResponse updatedStudent = studentService.updateStudent(studentId, updateStudentDto);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudentAndUser(studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Page<StudentResponse>> searchStudents(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<StudentResponse> studentsPage = studentService.searchStudents(search, pageable);
        return ResponseEntity.ok(studentsPage);
    }


    @GetMapping("/me/groups")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<GroupResponse>> getCurrentStudentGroups() {
        List<GroupResponse> groups = studentService.getCurrentStudentGroups();
        return ResponseEntity.ok(groups);
    }

    //todo grades
    @GetMapping("/me/grades")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<GradeResponse>> getCurrentStudentGrades(
            @RequestParam(required = false) Long courseId) {
        List<GradeResponse> grades = studentService.getCurrentStudentGrades(courseId);
        return ResponseEntity.ok(grades);
    }

    //todo attendance
    @GetMapping("/me/attendance")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<AttendanceResponse>> getCurrentStudentAttendance(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer meetingNumber) {
        List<AttendanceResponse> attendanceList = studentService.getCurrentStudentAttendance(groupId, courseId, meetingNumber);
        return ResponseEntity.ok(attendanceList);
    }
}