package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.exceptions.InvalidOperationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateGradeDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGradeDTO;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;
import pl.edu.agh.student_registration_system.repository.CourseRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.repository.GradeRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private TeacherService teacherService;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Student student;
    private Course course;
    private Teacher teacher;
    private Grade gradeInstanceForSetup;
    private CreateGradeDTO createGradeDTO;
    private UpdateGradeDTO updateGradeDTO;
    private User studentUser;
    private User teacherUser;
    private CourseGroup courseGroup;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        studentUser = new User(1L, "Student", "Test", "pass", "student@test.com", true, null, null, null);
        teacherUser = new User(2L, "Teacher", "Test", "pass", "teacher@test.com", true, null, null, null);

        student = new Student(1L, "123456", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        course = new Course(1L, "Test Course", "TC101", "Desc", 3, new HashSet<>(), new HashSet<>());
        teacher = new Teacher(1L, "Dr", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());

        courseGroup = new CourseGroup(1L, 1, 30, course, teacher, new HashSet<>(), Collections.emptyList());
        teacher.getTaughtGroups().add(courseGroup);

        enrollment = new Enrollment(1L, LocalDateTime.now(), student, courseGroup);
        student.getEnrollments().add(enrollment);

        gradeInstanceForSetup = new Grade(1L, "5.0", LocalDateTime.now(), "Good job", student, course, teacher);
        createGradeDTO = new CreateGradeDTO(1L, 1L, "4.5", "Okay");
        updateGradeDTO = new UpdateGradeDTO("5.0", "Excellent");
    }

    @Test
    void addGrade_Success_StudentEnrolledWithTeacher() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(enrollmentRepository.findByStudent(student)).thenReturn(Collections.singletonList(enrollment));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> {
            Grade savedGrade = invocation.getArgument(0);
            savedGrade.setGradeId(1L);
            return savedGrade;
        });

        GradeResponse response = gradeService.addGrade(createGradeDTO);

        assertNotNull(response);
        assertEquals(createGradeDTO.getGradeValue(), response.getGradeValue());
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void addGrade_Success_TeacherTeachesCourse_StudentNotDirectlyEnrolledWithTeacher() {
        Enrollment otherEnrollment = new Enrollment(2L, LocalDateTime.now(), student, new CourseGroup(2L, 2, 30, course, new Teacher(), new HashSet<>(), Collections.emptyList()));

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(enrollmentRepository.findByStudent(student)).thenReturn(Collections.singletonList(otherEnrollment));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> {
            Grade savedGrade = invocation.getArgument(0);
            savedGrade.setGradeId(1L);
            return savedGrade;
        });

        GradeResponse response = gradeService.addGrade(createGradeDTO);

        assertNotNull(response);
        assertEquals(createGradeDTO.getGradeValue(), response.getGradeValue());
        verify(gradeRepository).save(any(Grade.class));
    }


    @Test
    void addGrade_StudentNotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> gradeService.addGrade(createGradeDTO));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void addGrade_CourseNotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> gradeService.addGrade(createGradeDTO));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void addGrade_TeacherNotAuthorized_ThrowsInvalidOperationException() {
        Teacher otherTeacher = new Teacher(2L, "Prof", new User(), new HashSet<>(), new HashSet<>(), new HashSet<>());
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(otherTeacher);
        when(enrollmentRepository.findByStudent(student)).thenReturn(Collections.emptyList());

        assertThrows(InvalidOperationException.class, () -> gradeService.addGrade(createGradeDTO));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateGrade_Success() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(gradeInstanceForSetup));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GradeResponse response = gradeService.updateGrade(1L, updateGradeDTO);

        assertNotNull(response);
        assertEquals(updateGradeDTO.getGradeValue(), response.getGradeValue());
        assertEquals(updateGradeDTO.getComment(), response.getComment());
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void updateGrade_GradeNotFound_ThrowsResourceNotFoundException() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(1L, updateGradeDTO));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void deleteGrade_Success() {
        when(gradeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gradeRepository).deleteById(1L);
        gradeService.deleteGrade(1L);
        verify(gradeRepository).deleteById(1L);
    }

    @Test
    void deleteGrade_GradeNotFound_ThrowsResourceNotFoundException() {
        when(gradeRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> gradeService.deleteGrade(1L));
        verify(gradeRepository, never()).deleteById(1L);
    }

    @Test
    void getGradesByStudentAndCourse_Success() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeRepository.findAllByStudentAndCourseWithDetails(student, course)).thenReturn(Collections.singletonList(gradeInstanceForSetup));

        List<GradeResponse> responses = gradeService.getGradesByStudentAndCourse(1L, 1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(gradeInstanceForSetup.getGradeValue(), responses.get(0).getGradeValue());
    }

    @Test
    void getGradesByStudentAndCourse_NoGradesFound_ReturnsEmptyList() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeRepository.findAllByStudentAndCourseWithDetails(student, course)).thenReturn(Collections.emptyList());

        List<GradeResponse> responses = gradeService.getGradesByStudentAndCourse(1L, 1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }


    @Test
    void getGradesByStudentAndCourse_StudentNotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> gradeService.getGradesByStudentAndCourse(1L, 1L));
    }
}