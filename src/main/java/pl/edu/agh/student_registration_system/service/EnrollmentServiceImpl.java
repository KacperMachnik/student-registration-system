package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.EnrollmentConflictException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Enrollment;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.payload.response.EnrollmentResponse;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final StudentService studentService;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    @Transactional
    public EnrollmentResponse enrollCurrentUser(Long groupId) {
        log.info("Current student attempting to enroll in group ID: {}", groupId);

        Student student = studentService.findCurrentStudentEntity();

        CourseGroup group = findGroupByIdOrThrow(groupId);
        Course course = group.getCourse();

        if (enrollmentRepository.existsByStudentAndGroup_Course(student, course)) {
            log.warn("Enrollment failed for student {} in group {}: Already enrolled in course {}",
                    student.getStudentId(), groupId, course.getCourseCode());
            throw new EnrollmentConflictException("Student is already enrolled in a group for course: " + course.getCourseCode());
        }

        int currentEnrollmentCount = enrollmentRepository.countByGroup(group);
        if (currentEnrollmentCount >= group.getMaxCapacity()) {
            log.warn("Enrollment failed for student {} in group {}: Group capacity ({}) reached.",
                    student.getStudentId(), groupId, group.getMaxCapacity());
            throw new EnrollmentConflictException("Cannot enroll: Group capacity is full ("
                    + group.getMaxCapacity() + "/" + group.getMaxCapacity() + ").");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setGroup(group);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Student {} successfully enrolled in group {} (Enrollment ID: {})",
                student.getStudentId(), groupId, savedEnrollment.getEnrollmentId());
        return mapToEnrollmentResponse(savedEnrollment);
    }

    @Override
    @Transactional
    public void unenrollCurrentUser(Long groupId) {
        log.info("Current student attempting to unenroll from group ID: {}", groupId);

        Student student = studentService.findCurrentStudentEntity();
        Enrollment enrollment = enrollmentRepository.findByStudentAndGroup_CourseGroupId(student, groupId)
                .orElseThrow(() -> {
                    log.warn("Unenrollment failed: Student {} is not enrolled in group {}.", student.getStudentId(), groupId);
                    return new ResourceNotFoundException("Enrollment not found for current student in group", "groupId", groupId);
                });
        enrollmentRepository.delete(enrollment);
        log.info("Student {} successfully unenrolled from group {} (Enrollment ID: {})",
                student.getStudentId(), groupId, enrollment.getEnrollmentId());
    }

    @Override
    @Transactional
    public EnrollmentResponse enrollStudentById(Long studentId, Long groupId, boolean adminOverride) {
        log.info("Admin attempting to enroll student ID: {} into group ID: {}. Admin override: {}", studentId, groupId, adminOverride);

        Student student = studentService.findStudentById(studentId);

        CourseGroup group = findGroupByIdOrThrow(groupId);
        Course course = group.getCourse();

        if (enrollmentRepository.existsByStudentAndGroup_Course(student, course)) {
            log.warn("Admin enrollment failed for student {} in group {}: Already enrolled in course {}",
                    studentId, groupId, course.getCourseCode());
            throw new EnrollmentConflictException("Student is already enrolled in a group for course: " + course.getCourseCode());
        }

        int currentEnrollmentCount = enrollmentRepository.countByGroup(group);
        if (!adminOverride && currentEnrollmentCount >= group.getMaxCapacity()) {
            log.warn("Admin enrollment failed for student {} in group {}: Group capacity ({}) reached.",
                    studentId, groupId, group.getMaxCapacity());
            throw new EnrollmentConflictException("Cannot enroll: Group capacity is full ("
                    + group.getMaxCapacity() + "/" + group.getMaxCapacity() + "). Use admin override if necessary.");
        } else if (adminOverride && currentEnrollmentCount >= group.getMaxCapacity()) {
            log.warn("Admin override: Enrolling student {} into group {} despite capacity ({}/{}) being reached.",
                    studentId, groupId, currentEnrollmentCount, group.getMaxCapacity());
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setGroup(group);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Admin successfully enrolled student {} in group {} (Enrollment ID: {})",
                studentId, groupId, savedEnrollment.getEnrollmentId());

        return mapToEnrollmentResponse(savedEnrollment);
    }

    @Override
    @Transactional
    public void unenrollStudentById(Long studentId, Long groupId) {
        log.info("Admin attempting to unenroll student ID: {} from group ID: {}", studentId, groupId);
        Student student = studentService.findStudentById(studentId);

        Enrollment enrollment = enrollmentRepository.findByStudentAndGroup_CourseGroupId(student, groupId)
                .orElseThrow(() -> {
                    log.warn("Admin unenrollment failed: Student {} is not enrolled in group {}.", studentId, groupId);
                    return new ResourceNotFoundException("Enrollment not found for student " + studentId + " in group", "groupId", groupId);
                });

        enrollmentRepository.delete(enrollment);
        log.info("Admin successfully unenrolled student {} from group {} (Enrollment ID: {})",
                studentId, groupId, enrollment.getEnrollmentId());
    }

    private CourseGroup findGroupByIdOrThrow(Long groupId) {
        return courseGroupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> {
                    log.warn("CourseGroup not found with ID: {}", groupId);
                    return new ResourceNotFoundException("CourseGroup", "id", groupId);
                });
    }

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return new EnrollmentResponse(
                enrollment.getEnrollmentId(),
                enrollment.getStudent() != null ? enrollment.getStudent().getStudentId() : null,
                enrollment.getGroup() != null ? enrollment.getGroup().getCourseGroupId() : null,
                enrollment.getEnrollmentDate() != null ? enrollment.getEnrollmentDate().format(ISO_DATE_TIME_FORMATTER) : null
        );
    }
}