package pl.edu.agh.student_registration_system.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.model.Attendance;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.AttendanceRepository;
import pl.edu.agh.student_registration_system.security.service.AttendanceSecurityService;
import pl.edu.agh.student_registration_system.service.TeacherService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceSecurityServiceTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceSecurityService attendanceSecurityService;

    @Test
    void shouldAllowTeacherToUpdateOwnAttendance() {
        Long attendanceId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        Attendance attendance = new Attendance();
        attendance.setAttendanceId(attendanceId);
        attendance.setRecordedByTeacher(teacher);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));

        boolean result = attendanceSecurityService.canTeacherUpdateAttendance(attendanceId);

        assertTrue(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(attendanceRepository).findById(attendanceId);
    }

    @Test
    void shouldNotAllowTeacherToUpdateOthersAttendance() {
        Long attendanceId = 1L;
        Long currentTeacherId = 10L;
        Long otherTeacherId = 20L;

        Teacher currentTeacher = new Teacher();
        currentTeacher.setTeacherId(currentTeacherId);

        Teacher otherTeacher = new Teacher();
        otherTeacher.setTeacherId(otherTeacherId);

        Attendance attendance = new Attendance();
        attendance.setAttendanceId(attendanceId);
        attendance.setRecordedByTeacher(otherTeacher);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(currentTeacher);
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));

        boolean result = attendanceSecurityService.canTeacherUpdateAttendance(attendanceId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(attendanceRepository).findById(attendanceId);
    }

    @Test
    void shouldNotAllowUpdateWhenAttendanceNotFound() {
        Long attendanceId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        boolean result = attendanceSecurityService.canTeacherUpdateAttendance(attendanceId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(attendanceRepository).findById(attendanceId);
    }

    @Test
    void shouldNotAllowUpdateWhenNoTeacherRecorded() {
        Long attendanceId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        Attendance attendance = new Attendance();
        attendance.setAttendanceId(attendanceId);
        attendance.setRecordedByTeacher(null);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));

        boolean result = attendanceSecurityService.canTeacherUpdateAttendance(attendanceId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(attendanceRepository).findById(attendanceId);
    }
}
