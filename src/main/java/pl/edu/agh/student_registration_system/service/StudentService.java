package pl.edu.agh.student_registration_system.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.payload.dto.UpdateStudentDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.StudentResponse;

import java.util.List;

public interface StudentService {
    Student findCurrentStudentEntity();

    Student findStudentById(Long studentId);

    StudentResponse getStudentResponseById(Long studentId);

    StudentResponse getCurrentStudentResponse();

    StudentResponse updateStudent(Long studentId, UpdateStudentDTO updateStudentDto);

    void deleteStudentAndUser(Long studentId);

    Page<StudentResponse> searchStudents(String search, Pageable pageable);

    List<GroupResponse> getCurrentStudentGroups();

    List<GradeResponse> getCurrentStudentGrades(Long courseId);

    List<AttendanceResponse> getCurrentStudentAttendance(Long groupId, Long courseId, Integer meetingNumber);

    StudentResponse mapToStudentResponse(Student student);
}
