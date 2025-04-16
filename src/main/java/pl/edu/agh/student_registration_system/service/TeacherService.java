package pl.edu.agh.student_registration_system.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.payload.dto.UpdateTeacherDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.TeacherResponse;

import java.util.List;

public interface TeacherService {
    TeacherResponse getTeacherResponseById(Long teacherId);

    TeacherResponse getCurrentTeacherResponse();

    TeacherResponse updateTeacher(Long teacherId, @Valid UpdateTeacherDTO updateTeacherDto);

    void deleteTeacherAndUser(Long teacherId);

    Page<TeacherResponse> searchTeachers(String search, Pageable pageable);

    List<CourseResponse> getCurrentTeacherCourses();

    List<GroupResponse> getCurrentTeacherGroups(Long courseId);

    Teacher findById(Long teacherId);

    Teacher findCurrentTeacherEntity();
}
