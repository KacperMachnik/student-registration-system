package pl.edu.agh.student_registration_system.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.security.service.StudentSecurityService;
import pl.edu.agh.student_registration_system.service.TeacherService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentSecurityServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherService teacherService;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @InjectMocks
    private StudentSecurityService studentSecurityService;

    @Test
    void shouldReturnTrueWhenTeacherAllowedToViewStudent() {
        Long studentId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        Student student = new Student();
        student.setStudentId(studentId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher, student)).thenReturn(true);

        boolean result = studentSecurityService.isTeacherAllowedToViewStudent(studentId);

        assertTrue(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(studentRepository).findById(studentId);
        verify(courseGroupRepository).existsByTeacherAndEnrollmentsStudent(teacher, student);
    }

    @Test
    void shouldReturnFalseWhenTeacherNotAllowedToViewStudent() {
        Long studentId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        Student student = new Student();
        student.setStudentId(studentId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher, student)).thenReturn(false);

        boolean result = studentSecurityService.isTeacherAllowedToViewStudent(studentId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(studentRepository).findById(studentId);
        verify(courseGroupRepository).existsByTeacherAndEnrollmentsStudent(teacher, student);
    }

    @Test
    void shouldReturnFalseWhenStudentNotFound() {
        Long studentId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        boolean result = studentSecurityService.isTeacherAllowedToViewStudent(studentId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(studentRepository).findById(studentId);
        verify(courseGroupRepository, never()).existsByTeacherAndEnrollmentsStudent(any(), any());
    }

    @Test
    void shouldReturnFalseWhenTeacherServiceThrowsException() {
        Long studentId = 1L;

        when(teacherService.findCurrentTeacherEntity()).thenThrow(new RuntimeException("Test exception"));

        boolean result = studentSecurityService.isTeacherAllowedToViewStudent(studentId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(studentRepository, never()).findById(anyLong());
        verify(courseGroupRepository, never()).existsByTeacherAndEnrollmentsStudent(any(), any());
    }

    @Test
    void shouldReturnFalseWhenStudentRepositoryThrowsException() {
        Long studentId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(studentId)).thenThrow(new RuntimeException("Test exception"));

        boolean result = studentSecurityService.isTeacherAllowedToViewStudent(studentId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(studentRepository).findById(studentId);
        verify(courseGroupRepository, never()).existsByTeacherAndEnrollmentsStudent(any(), any());
    }

    @Test
    void shouldReturnFalseWhenCourseGroupRepositoryThrowsException() {
        Long studentId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        Student student = new Student();
        student.setStudentId(studentId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher, student))
                .thenThrow(new RuntimeException("Test exception"));

        boolean result = studentSecurityService.isTeacherAllowedToViewStudent(studentId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(studentRepository).findById(studentId);
        verify(courseGroupRepository).existsByTeacherAndEnrollmentsStudent(teacher, student);
    }
}
