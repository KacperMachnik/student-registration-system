package pl.edu.agh.student_registration_system.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.service.TeacherService;

@Component("studentSecurityService")
@RequiredArgsConstructor
@Slf4j
public class StudentSecurityService {

    private final StudentRepository studentRepository;
    private final TeacherService teacherService;
    private final CourseGroupRepository courseGroupRepository;

    @Transactional(readOnly = true)
    public boolean isTeacherAllowedToViewStudent(Long studentId) {
        try {
            log.info("Checking whether teacher is allowed to view student");
            Teacher currentTeacher = teacherService.findCurrentTeacherEntity();

            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                log.warn("Attempt to check access for non-existent student ID: {}", studentId);
                return false;
            }

            log.info("Checking whether teacher with Id {} teaches student with id {}", currentTeacher.getTeacherId(), student.getStudentId());
            boolean isAllowed = courseGroupRepository.existsByTeacherAndEnrollmentsStudent(currentTeacher, student);

            log.debug("Access check for teacher {} to view student {}: {}", currentTeacher.getTeacherId(), studentId, isAllowed);
            return isAllowed;

        } catch (Exception e) {
            log.error("Error during access check in isTeacherAllowedToViewStudent for studentId {}: {}", studentId, e.getMessage());
            return false;
        }
    }
}