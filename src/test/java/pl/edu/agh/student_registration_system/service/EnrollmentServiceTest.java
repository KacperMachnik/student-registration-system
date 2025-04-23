package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private Student testStudent;
    private CourseGroup testGroup;
    private Course testCourse;
    private Enrollment testEnrollment;
    private LocalDateTime enrollmentDate;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setStudentId(1L);
        testStudent.setIndexNumber("123456");

        testCourse = new Course();
        testCourse.setCourseId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");

        testGroup = new CourseGroup();
        testGroup.setCourseGroupId(1L);
        testGroup.setGroupNumber(1);
        testGroup.setMaxCapacity(30);
        testGroup.setCourse(testCourse);

        enrollmentDate = LocalDateTime.now();
        testEnrollment = new Enrollment();
        testEnrollment.setEnrollmentId(1L);
        testEnrollment.setStudent(testStudent);
        testEnrollment.setGroup(testGroup);
        testEnrollment.setEnrollmentDate(enrollmentDate);
    }

    @Test
    void enrollCurrentUser_ShouldReturnEnrollmentResponse_WhenEnrollmentIsSuccessful() {
        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(testStudent, testCourse)).thenReturn(false);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentResponse response = enrollmentService.enrollCurrentUser(1L);

        assertNotNull(response);
        assertEquals(1L, response.getEnrollmentId());
        assertEquals(1L, response.getStudentId());
        assertEquals(1L, response.getGroupId());
        assertEquals(enrollmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), response.getEnrollmentDate());

        verify(studentService).findCurrentStudentEntity();
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).existsByStudentAndGroup_Course(testStudent, testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollCurrentUser_ShouldThrowResourceNotFoundException_WhenGroupDoesNotExist() {
        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.enrollCurrentUser(1L));

        verify(studentService).findCurrentStudentEntity();
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository, never()).existsByStudentAndGroup_Course(any(), any());
        verify(enrollmentRepository, never()).countByGroup(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollCurrentUser_ShouldThrowEnrollmentConflictException_WhenAlreadyEnrolledInCourse() {
        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(testStudent, testCourse)).thenReturn(true);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollCurrentUser(1L));

        verify(studentService).findCurrentStudentEntity();
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).existsByStudentAndGroup_Course(testStudent, testCourse);
        verify(enrollmentRepository, never()).countByGroup(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollCurrentUser_ShouldThrowEnrollmentConflictException_WhenGroupIsFull() {
        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(testStudent, testCourse)).thenReturn(false);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(30);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollCurrentUser(1L));

        verify(studentService).findCurrentStudentEntity();
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).existsByStudentAndGroup_Course(testStudent, testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void unenrollCurrentUser_ShouldDeleteEnrollment_WhenEnrollmentExists() {
        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(testStudent, 1L)).thenReturn(Optional.of(testEnrollment));
        doNothing().when(enrollmentRepository).delete(testEnrollment);

        enrollmentService.unenrollCurrentUser(1L);

        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository).findByStudentAndGroup_CourseGroupId(testStudent, 1L);
        verify(enrollmentRepository).delete(testEnrollment);
    }

    @Test
    void unenrollCurrentUser_ShouldThrowResourceNotFoundException_WhenEnrollmentDoesNotExist() {
        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(testStudent, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.unenrollCurrentUser(1L));

        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository).findByStudentAndGroup_CourseGroupId(testStudent, 1L);
        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void enrollStudentById_ShouldReturnEnrollmentResponse_WhenEnrollmentIsSuccessful() {
        when(studentService.findStudentById(1L)).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(testStudent, testCourse)).thenReturn(false);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentResponse response = enrollmentService.enrollStudentById(1L, 1L, false);

        assertNotNull(response);
        assertEquals(1L, response.getEnrollmentId());
        assertEquals(1L, response.getStudentId());
        assertEquals(1L, response.getGroupId());

        verify(studentService).findStudentById(1L);
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).existsByStudentAndGroup_Course(testStudent, testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudentById_ShouldAllowEnrollmentWithAdminOverride_WhenGroupIsFull() {
        when(studentService.findStudentById(1L)).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(testStudent, testCourse)).thenReturn(false);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(30);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentResponse response = enrollmentService.enrollStudentById(1L, 1L, true);

        assertNotNull(response);
        assertEquals(1L, response.getEnrollmentId());

        verify(studentService).findStudentById(1L);
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).existsByStudentAndGroup_Course(testStudent, testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudentById_ShouldThrowEnrollmentConflictException_WhenGroupIsFullWithoutOverride() {
        when(studentService.findStudentById(1L)).thenReturn(testStudent);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.existsByStudentAndGroup_Course(testStudent, testCourse)).thenReturn(false);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(30);

        assertThrows(EnrollmentConflictException.class, () -> enrollmentService.enrollStudentById(1L, 1L, false));

        verify(studentService).findStudentById(1L);
        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).existsByStudentAndGroup_Course(testStudent, testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void unenrollStudentById_ShouldDeleteEnrollment_WhenEnrollmentExists() {
        when(studentService.findStudentById(1L)).thenReturn(testStudent);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(testStudent, 1L)).thenReturn(Optional.of(testEnrollment));
        doNothing().when(enrollmentRepository).delete(testEnrollment);

        enrollmentService.unenrollStudentById(1L, 1L);

        verify(studentService).findStudentById(1L);
        verify(enrollmentRepository).findByStudentAndGroup_CourseGroupId(testStudent, 1L);
        verify(enrollmentRepository).delete(testEnrollment);
    }

    @Test
    void unenrollStudentById_ShouldThrowResourceNotFoundException_WhenEnrollmentDoesNotExist() {
        when(studentService.findStudentById(1L)).thenReturn(testStudent);
        when(enrollmentRepository.findByStudentAndGroup_CourseGroupId(testStudent, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.unenrollStudentById(1L, 1L));

        verify(studentService).findStudentById(1L);
        verify(enrollmentRepository).findByStudentAndGroup_CourseGroupId(testStudent, 1L);
        verify(enrollmentRepository, never()).delete(any());
    }
}
