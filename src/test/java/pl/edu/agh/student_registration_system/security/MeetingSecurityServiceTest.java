package pl.edu.agh.student_registration_system.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;
import pl.edu.agh.student_registration_system.security.service.MeetingSecurityService;
import pl.edu.agh.student_registration_system.service.TeacherService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingSecurityServiceTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private MeetingSecurityService meetingSecurityService;

    @Test
    void shouldReturnTrueWhenTeacherForMeeting() {
        Long meetingId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        CourseGroup group = new CourseGroup();
        group.setTeacher(teacher);

        Meeting meeting = new Meeting();
        meeting.setMeetingId(meetingId);
        meeting.setGroup(group);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        boolean result = meetingSecurityService.isTeacherForMeeting(meetingId);

        assertTrue(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(meetingRepository).findById(meetingId);
    }

    @Test
    void shouldReturnFalseWhenNotTeacherForMeeting() {
        Long meetingId = 1L;
        Long currentTeacherId = 10L;
        Long otherTeacherId = 20L;

        Teacher currentTeacher = new Teacher();
        currentTeacher.setTeacherId(currentTeacherId);

        Teacher otherTeacher = new Teacher();
        otherTeacher.setTeacherId(otherTeacherId);

        CourseGroup group = new CourseGroup();
        group.setTeacher(otherTeacher);

        Meeting meeting = new Meeting();
        meeting.setMeetingId(meetingId);
        meeting.setGroup(group);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(currentTeacher);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        boolean result = meetingSecurityService.isTeacherForMeeting(meetingId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(meetingRepository).findById(meetingId);
    }

    @Test
    void shouldReturnFalseWhenMeetingNotFound() {
        Long meetingId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        boolean result = meetingSecurityService.isTeacherForMeeting(meetingId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(meetingRepository).findById(meetingId);
    }

    @Test
    void shouldReturnFalseWhenMeetingHasNoGroup() {
        Long meetingId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        Meeting meeting = new Meeting();
        meeting.setMeetingId(meetingId);
        meeting.setGroup(null);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        boolean result = meetingSecurityService.isTeacherForMeeting(meetingId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(meetingRepository).findById(meetingId);
    }

    @Test
    void shouldReturnFalseWhenGroupHasNoTeacher() {
        Long meetingId = 1L;
        Long teacherId = 10L;

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);

        CourseGroup group = new CourseGroup();
        group.setTeacher(null);

        Meeting meeting = new Meeting();
        meeting.setMeetingId(meetingId);
        meeting.setGroup(group);

        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        boolean result = meetingSecurityService.isTeacherForMeeting(meetingId);

        assertFalse(result);
        verify(teacherService).findCurrentTeacherEntity();
        verify(meetingRepository).findById(meetingId);
    }
}
