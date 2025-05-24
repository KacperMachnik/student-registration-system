package pl.edu.agh.student_registration_system.service;

import jakarta.validation.Valid;
import pl.edu.agh.student_registration_system.payload.dto.CreateGradeDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGradeDTO;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;

import java.util.List;

public interface GradeService {
    GradeResponse addGrade(@Valid CreateGradeDTO createGradeDto);

    GradeResponse updateGrade(Long gradeId, @Valid UpdateGradeDTO updateGradeDto);

    void deleteGrade(Long gradeId);

    List<GradeResponse> getGradesByStudentAndCourse(Long studentId, Long courseId);
}
