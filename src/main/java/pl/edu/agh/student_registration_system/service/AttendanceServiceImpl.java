package pl.edu.agh.student_registration_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.student_registration_system.payload.dto.RecordAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;

import java.util.List;

@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {


    @Override
    public List<AttendanceResponse> recordAttendanceForMeeting(Long meetingId, RecordAttendanceDTO recordAttendanceDto) {
        return null;
    }

    @Override
    public List<AttendanceResponse> getAttendanceByMeetingId(Long meetingId) {
        return null;
    }


    @Override
    public AttendanceResponse updateSingleAttendance(Long attendanceId, UpdateAttendanceDTO updateAttendanceDto) {
        return null;
    }
}