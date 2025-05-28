package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.exceptions.InvalidOperationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.AttendanceRecordDTO;
import pl.edu.agh.student_registration_system.payload.dto.RecordAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;
import pl.edu.agh.student_registration_system.repository.AttendanceRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherService teacherService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Meeting meeting;
    private Student student1;
    private Teacher teacher;
    private CourseGroup courseGroup;
    private User studentUser;
    private User teacherUser;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @BeforeEach
    void setUp() {
        studentUser = new User(1L, "Student", "Test", "pass", "student@test.com", true, null, null, null);
        teacherUser = new User(2L, "Teacher", "Test", "pass", "teacher@test.com", true, null, null, null);

        student1 = new Student(1L, "123456", studentUser, Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        teacher = new Teacher(1L, "Dr", teacherUser, Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        courseGroup = new CourseGroup(1L, 1, 30, null, teacher, Collections.emptySet(), Collections.emptyList());
        meeting = new Meeting(1L, 1, LocalDateTime.now(), "Topic 1", courseGroup, Collections.emptySet());
    }

    @Test
    void recordAttendanceForMeeting_Success_NewAttendance() {
        Long meetingId = 1L;
        AttendanceRecordDTO recordDTO = new AttendanceRecordDTO(1L, "PRESENT");
        RecordAttendanceDTO recordAttendanceDto = new RecordAttendanceDTO(Collections.singletonList(recordDTO));

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student1, courseGroup.getCourseGroupId())).thenReturn(true);
        when(attendanceRepository.findByMeetingAndStudent(meeting, student1)).thenReturn(Optional.empty());
        when(attendanceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<AttendanceResponse> responses = attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("PRESENT", responses.get(0).getStatus());
        assertEquals(student1.getStudentId(), responses.get(0).getStudent().getStudentId());
        assertEquals(meeting.getMeetingId(), responses.get(0).getMeeting().getMeetingId());

        verify(attendanceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void recordAttendanceForMeeting_Success_UpdateAttendance() {
        Long meetingId = 1L;
        AttendanceRecordDTO recordDTO = new AttendanceRecordDTO(1L, "ABSENT");
        RecordAttendanceDTO recordAttendanceDto = new RecordAttendanceDTO(Collections.singletonList(recordDTO));
        Attendance existingAttendance = new Attendance(1L, AttendanceStatus.PRESENT, meeting, student1, teacher);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student1, courseGroup.getCourseGroupId())).thenReturn(true);
        when(attendanceRepository.findByMeetingAndStudent(meeting, student1)).thenReturn(Optional.of(existingAttendance));
        when(attendanceRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Attendance> savedList = invocation.getArgument(0);
            savedList.get(0).setAttendanceId(1L);
            return savedList;
        });


        List<AttendanceResponse> responses = attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("ABSENT", responses.get(0).getStatus());
        assertEquals(existingAttendance.getAttendanceId(), responses.get(0).getAttendanceId());

        verify(attendanceRepository, times(1)).saveAll(argThat(list ->
                StreamSupport.stream(list.spliterator(), false)
                        .anyMatch(att -> att.getStatus() == AttendanceStatus.ABSENT)
        ));
    }

    @Test
    void recordAttendanceForMeeting_MeetingNotFound_ThrowsResourceNotFoundException() {
        Long meetingId = 1L;
        RecordAttendanceDTO recordAttendanceDto = new RecordAttendanceDTO(Collections.emptyList());
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto));
    }

    @Test
    void recordAttendanceForMeeting_StudentNotFound_ThrowsResourceNotFoundException() {
        Long meetingId = 1L;
        AttendanceRecordDTO recordDTO = new AttendanceRecordDTO(99L, "PRESENT");
        RecordAttendanceDTO recordAttendanceDto = new RecordAttendanceDTO(Collections.singletonList(recordDTO));

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto));
    }

    @Test
    void recordAttendanceForMeeting_StudentNotEnrolled_ThrowsInvalidOperationException() {
        Long meetingId = 1L;
        AttendanceRecordDTO recordDTO = new AttendanceRecordDTO(1L, "PRESENT");
        RecordAttendanceDTO recordAttendanceDto = new RecordAttendanceDTO(Collections.singletonList(recordDTO));

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student1, courseGroup.getCourseGroupId())).thenReturn(false);

        assertThrows(InvalidOperationException.class,
                () -> attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto));
    }

    @Test
    void recordAttendanceForMeeting_InvalidStatus_ThrowsInvalidOperationException() {
        Long meetingId = 1L;
        AttendanceRecordDTO recordDTO = new AttendanceRecordDTO(1L, "INVALID_STATUS");
        RecordAttendanceDTO recordAttendanceDto = new RecordAttendanceDTO(Collections.singletonList(recordDTO));

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(teacherService.findCurrentTeacherEntity()).thenReturn(teacher);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student1, courseGroup.getCourseGroupId())).thenReturn(true);
        when(attendanceRepository.findByMeetingAndStudent(meeting, student1)).thenReturn(Optional.empty());


        assertThrows(InvalidOperationException.class,
                () -> attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto));
    }


    @Test
    void getAttendanceByMeetingId_Success() {
        Long meetingId = 1L;
        Attendance attendance = new Attendance(1L, AttendanceStatus.PRESENT, meeting, student1, teacher);
        when(meetingRepository.existsById(meetingId)).thenReturn(true);
        when(attendanceRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(attendance));

        List<AttendanceResponse> responses = attendanceService.getAttendanceByMeetingId(meetingId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(AttendanceStatus.PRESENT.name(), responses.get(0).getStatus());
        assertEquals(student1.getStudentId(), responses.get(0).getStudent().getStudentId());
        assertEquals(meeting.getMeetingId(), responses.get(0).getMeeting().getMeetingId());
        assertEquals(meeting.getMeetingDate().format(ISO_DATE_TIME_FORMATTER), responses.get(0).getMeeting().getMeetingDate());


        verify(attendanceRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getAttendanceByMeetingId_MeetingNotFound_ThrowsResourceNotFoundException() {
        Long meetingId = 1L;
        when(meetingRepository.existsById(meetingId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> attendanceService.getAttendanceByMeetingId(meetingId));
    }

    @Test
    void updateSingleAttendance_Success() {
        Long attendanceId = 1L;
        UpdateAttendanceDTO updateDto = new UpdateAttendanceDTO("ABSENT");
        Attendance existingAttendance = new Attendance(attendanceId, AttendanceStatus.PRESENT, meeting, student1, teacher);
        Attendance updatedAttendance = new Attendance(attendanceId, AttendanceStatus.ABSENT, meeting, student1, teacher);

        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(existingAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(updatedAttendance);

        AttendanceResponse response = attendanceService.updateSingleAttendance(attendanceId, updateDto);

        assertNotNull(response);
        assertEquals(AttendanceStatus.ABSENT.name(), response.getStatus());
        assertEquals(attendanceId, response.getAttendanceId());

        verify(attendanceRepository, times(1)).save(argThat(att -> att.getStatus() == AttendanceStatus.ABSENT));
    }

    @Test
    void updateSingleAttendance_AttendanceNotFound_ThrowsResourceNotFoundException() {
        Long attendanceId = 1L;
        UpdateAttendanceDTO updateDto = new UpdateAttendanceDTO("ABSENT");
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.updateSingleAttendance(attendanceId, updateDto));
    }

    @Test
    void updateSingleAttendance_InvalidStatus_ThrowsInvalidOperationException() {
        Long attendanceId = 1L;
        UpdateAttendanceDTO updateDto = new UpdateAttendanceDTO("INVALID_STATUS");
        Attendance existingAttendance = new Attendance(attendanceId, AttendanceStatus.PRESENT, meeting, student1, teacher);

        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(existingAttendance));

        assertThrows(InvalidOperationException.class,
                () -> attendanceService.updateSingleAttendance(attendanceId, updateDto));
    }

}