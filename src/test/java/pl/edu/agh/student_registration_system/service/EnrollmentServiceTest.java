package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.exceptions.EnrollmentConflictException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.response.EnrollmentResponse;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private Student student;
    private Course course;
    private CourseGroup courseGroup;
    private Enrollment enrollment;
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @BeforeEach
    void setUp() {
        User user = new User(1L, "Student", "Test", "pass", "student@test.com", true, null, null, null);
        student = new Student(1L, "123456", user, null, null, null);
        course = new Course(1L, "Test Course", "TC101", "Desc", 3, null, null);
        courseGroup = new CourseGroup(1L, 1, 30, course, null, null, null);
        enrollment = new Enrollment(1L, LocalDateTime.now(), student, courseGroup);
    }

    @Test
    void enrollCurrentUser_Success() {
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(0);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.enrollCurrentUser(1L);

        assertNotNull(response);
        assertEquals(enrollment.getEnrollmentId(), response.getEnrollmentId());
        assertEquals(student.getStudentId(), response.getStudentId());
        assertEquals(courseGroup.getCourseGroupId(), response.getGroupId());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollCurrentUser_AlreadyEnrolledInCourse_ThrowsEnrollmentConflictException() {
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(true);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollCurrentUser(1L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollCurrentUser_GroupFull_ThrowsEnrollmentConflictException() {
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(30);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollCurrentUser(1L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollCurrentUser_GroupNotFound_ThrowsResourceNotFoundException() {
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.enrollCurrentUser(1L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void unenrollCurrentUser_Success() {
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(student, 1L)).thenReturn(Optional.of(enrollment));
        doNothing().when(enrollmentRepository).delete(enrollment);

        enrollmentService.unenrollCurrentUser(1L);

        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenrollCurrentUser_NotEnrolled_ThrowsResourceNotFoundException() {
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(student, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.unenrollCurrentUser(1L));
        verify(enrollmentRepository, never()).delete(any(Enrollment.class));
    }

    @Test
    void enrollStudentById_Success_NoAdminOverride_CapacityOk() {
        when(studentService.findStudentById(1L)).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(0);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.enrollStudentById(1L, 1L, false);

        assertNotNull(response);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudentById_Success_AdminOverride_CapacityFull() {
        when(studentService.findStudentById(1L)).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(30);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.enrollStudentById(1L, 1L, true);

        assertNotNull(response);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }


    @Test
    void enrollStudentById_Fail_NoAdminOverride_CapacityFull() {
        when(studentService.findStudentById(1L)).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(30);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollStudentById(1L, 1L, false));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollStudentById_AlreadyEnrolled_ThrowsEnrollmentConflictException() {
        when(studentService.findStudentById(1L)).thenReturn(student);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(student, course)).thenReturn(true);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollStudentById(1L, 1L, false));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }


    @Test
    void unenrollStudentById_Success() {
        when(studentService.findStudentById(1L)).thenReturn(student);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(student, 1L)).thenReturn(Optional.of(enrollment));
        doNothing().when(enrollmentRepository).delete(enrollment);

        enrollmentService.unenrollStudentById(1L, 1L);

        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenrollStudentById_EnrollmentNotFound_ThrowsResourceNotFoundException() {
        when(studentService.findStudentById(1L)).thenReturn(student);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(student, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.unenrollStudentById(1L, 1L));
        verify(enrollmentRepository, never()).delete(any(Enrollment.class));
    }
}