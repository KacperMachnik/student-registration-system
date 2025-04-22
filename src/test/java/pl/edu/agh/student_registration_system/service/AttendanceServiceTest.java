package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.payload.dto.RecordAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Test
    void shouldReturnNullWhenRecordAttendanceForMeeting() {
        Long meetingId = 1L;
        RecordAttendanceDTO recordAttendanceDTO = new RecordAttendanceDTO();

        List<AttendanceResponse> result = attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDTO);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenGetAttendanceByMeetingId() {
        Long meetingId = 1L;

        List<AttendanceResponse> result = attendanceService.getAttendanceByMeetingId(meetingId);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenUpdateSingleAttendance() {
        Long attendanceId = 1L;
        UpdateAttendanceDTO updateAttendanceDTO = new UpdateAttendanceDTO();

        AttendanceResponse result = attendanceService.updateSingleAttendance(attendanceId, updateAttendanceDTO);

        assertNull(result);
    }
}
