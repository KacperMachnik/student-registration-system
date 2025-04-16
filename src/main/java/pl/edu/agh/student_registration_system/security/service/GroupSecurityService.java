package pl.edu.agh.student_registration_system.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.service.StudentService;
import pl.edu.agh.student_registration_system.service.TeacherService;

@Component("groupSecurityService")
@RequiredArgsConstructor
@Slf4j
public class GroupSecurityService {

    private final CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final TeacherService teacherService;


    @Transactional(readOnly = true)
    public boolean isTeacherOfGroup(Long groupId) {
        log.debug("Checking if current user is teacher of group {}", groupId);
        log.error("WEIRD if got here");
        try {
            Teacher currentTeacher = teacherService.findCurrentTeacherEntity();

            CourseGroup group = courseGroupRepository.findById(groupId).orElse(null);

            boolean isTeacher = group != null
                    && group.getTeacher() != null
                    && group.getTeacher().getTeacherId().equals(currentTeacher.getTeacherId());

            log.debug("Access check isTeacherOfGroup for group {}: {}", groupId, isTeacher);
            return isTeacher;
        } catch (ResourceNotFoundException e) {
            log.warn("isTeacherOfGroup check failed for group {}: Current user is not a teacher or teacher profile not found.", groupId);
            return false;
        } catch (Exception e) {
            log.error("Error during isTeacherOfGroup check for group {}: {}", groupId, e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInGroup(Long groupId) {
        log.debug("Checking if current user is enrolled in group {}", groupId);
        try {
            Student currentStudent = studentService.findCurrentStudentEntity();

            boolean isEnrolled = enrollmentRepository.existsByStudentAndGroup_CourseGroupId(currentStudent, groupId);

            log.debug("Access check isStudentEnrolledInGroup for group {}: {}", groupId, isEnrolled);
            return isEnrolled;
        } catch (ResourceNotFoundException e) {
            log.warn("isStudentEnrolledInGroup check failed for group {}: Current user is not a student or student profile not found.", groupId);
            return false;
        } catch (Exception e) {
            log.error("Error during isStudentEnrolledInGroup check for group {}: {}", groupId, e.getMessage());
            return false;
        }
    }
}