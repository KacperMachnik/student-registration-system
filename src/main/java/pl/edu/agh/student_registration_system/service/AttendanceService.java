package pl.edu.agh.student_registration_system.service;

import jakarta.validation.Valid;
import pl.edu.agh.student_registration_system.payload.dto.RecordAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;

import java.util.List;

public interface AttendanceService {
    List<AttendanceResponse> recordAttendanceForMeeting(Long meetingId, @Valid RecordAttendanceDTO recordAttendanceDto);

    List<AttendanceResponse> getAttendanceByMeetingId(Long meetingId);

    AttendanceResponse updateSingleAttendance(Long attendanceId, @Valid UpdateAttendanceDTO updateAttendanceDto);
}
