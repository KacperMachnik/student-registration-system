package pl.edu.agh.student_registration_system.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.security.service.GroupSecurityService;
import pl.edu.agh.student_registration_system.service.StudentService;
import pl.edu.agh.student_registration_system.service.TeacherService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupSecurityServiceTest {

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private GroupSecurityService groupSecurityService;

    @Test
    void shouldReturnTrueWhenTeacherOfGroup() {
        Long groupId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        CourseGroup group = new CourseGroup();
        group.setCourseGroupId(groupId);
        group.setTeacher(teacher);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(courseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        boolean result = groupSecurityService.isTeacherOfGroup(groupId);

        assertTrue(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(courseGroupRepository).findById(groupId);
    }

    @Test
    void shouldReturnFalseWhenNotTeacherOfGroup() {
        Long groupId = 1L;
        Long currentTeacherId = 10L;
        Long otherTeacherId = 20L;

        Teacher currentTeacher = new Teacher();
        currentTeacher.setTeacherId(currentTeacherId);

        Teacher otherTeacher = new Teacher();
        otherTeacher.setTeacherId(otherTeacherId);

        CourseGroup group = new CourseGroup();
        group.setCourseGroupId(groupId);
        group.setTeacher(otherTeacher);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(currentTeacher);
        when(courseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        boolean result = groupSecurityService.isTeacherOfGroup(groupId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(courseGroupRepository).findById(groupId);
    }

    @Test
    void shouldReturnFalseWhenGroupNotFound() {
        Long groupId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(courseGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        boolean result = groupSecurityService.isTeacherOfGroup(groupId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(courseGroupRepository).findById(groupId);
    }

    @Test
    void shouldReturnFalseWhenGroupHasNoTeacher() {
        Long groupId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        CourseGroup group = new CourseGroup();
        group.setCourseGroupId(groupId);
        group.setTeacher(null);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(courseGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        boolean result = groupSecurityService.isTeacherOfGroup(groupId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(courseGroupRepository).findById(groupId);
    }

    @Test
    void shouldReturnFalseWhenTeacherServiceThrowsException() {
        Long groupId = 1L;

        when(teacherService.findCurrentTeacherEntity()).thenThrow(mock(ResourceNotFoundException.class));

        boolean result = groupSecurityService.isTeacherOfGroup(groupId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(courseGroupRepository, never()).findById(anyLong());
    }

    @Test
    void shouldReturnTrueWhenStudentEnrolledInGroup() {
        Long groupId = 1L;

        Student student = new Student();
        student.setStudentId(10L);

        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student, groupId)).thenReturn(true);

        boolean result = groupSecurityService.isStudentEnrolledInGroup(groupId);

        assertTrue(result);
        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository).existsByStudentAndGroup_CourseGroupId(student, groupId);
    }

    @Test
    void shouldReturnFalseWhenStudentNotEnrolledInGroup() {
        Long groupId = 1L;

        Student student = new Student();
        student.setStudentId(10L);

        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student, groupId)).thenReturn(false);

        boolean result = groupSecurityService.isStudentEnrolledInGroup(groupId);

        assertFalse(result);
        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository).existsByStudentAndGroup_CourseGroupId(student, groupId);
    }

    @Test
    void shouldReturnFalseWhenStudentServiceThrowsException() {
        Long groupId = 1L;

        when(studentService.findCurrentStudentEntity()).thenThrow(mock(ResourceNotFoundException.class));

        boolean result = groupSecurityService.isStudentEnrolledInGroup(groupId);

        assertFalse(result);
        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository, never()).existsByStudentAndGroup_CourseGroupId(any(), anyLong());
    }
}
