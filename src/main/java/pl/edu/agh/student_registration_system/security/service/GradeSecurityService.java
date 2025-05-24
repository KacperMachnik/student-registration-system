package pl.edu.agh.student_registration_system.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.GradeRepository;
import pl.edu.agh.student_registration_system.service.TeacherService;

@Service("gradeSecurityService")
@RequiredArgsConstructor
public class GradeSecurityService {

    private final TeacherService teacherService;
    private final GradeRepository gradeRepository;

    @Transactional(readOnly = true)
    public boolean isTeacherIssuerOfGrade(Long gradeId) {
        Teacher currentTeacher = teacherService.findCurrentTeacherEntity();
        return gradeRepository.findById(gradeId)
                .map(grade -> grade.getTeacher() != null && grade.getTeacher().getTeacherId().equals(currentTeacher.getTeacherId()))
                .orElse(false);
    }
}