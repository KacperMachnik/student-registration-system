package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.dto.DefineMeetingDTO;
import pl.edu.agh.student_registration_system.payload.dto.RecordAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.response.AttendanceResponse;
import pl.edu.agh.student_registration_system.payload.response.MeetingResponse;
import pl.edu.agh.student_registration_system.service.AttendanceService;
import pl.edu.agh.student_registration_system.service.MeetingService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeetingAttendanceController {

    private final MeetingService meetingService;
    private final AttendanceService attendanceService;


    @PostMapping("/groups/{groupId}/meetings")
    @PreAuthorize("hasAuthority('DEANERY_STAFF') or (hasAuthority('TEACHER') and @groupSecurityService.isTeacherOfGroup(#groupId))")
    public ResponseEntity<List<MeetingResponse>> defineMeetings(@PathVariable Long groupId, @Valid @RequestBody DefineMeetingDTO defineMeetingDto) {
        List<MeetingResponse> meetings = meetingService.defineMeetingsForGroup(groupId, defineMeetingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(meetings);
    }

    @GetMapping("/groups/{groupId}/meetings")
    @PreAuthorize("hasAuthority('DEANERY_STAFF') or (hasAuthority('TEACHER') and @groupSecurityService.isTeacherOfGroup(#groupId)) or (hasAuthority('STUDENT') and @groupSecurityService.isStudentEnrolledInGroup(#groupId))")
    public ResponseEntity<List<MeetingResponse>> getMeetingsForGroup(@PathVariable Long groupId) {
        List<MeetingResponse> meetings = meetingService.getMeetingsByGroupId(groupId);
        return ResponseEntity.ok(meetings);
    }

    @PostMapping("/meetings/{meetingId}/attendance")
    @PreAuthorize("hasAuthority('TEACHER') and @meetingSecurityService.isTeacherForMeeting(#meetingId)")
    public ResponseEntity<List<AttendanceResponse>> recordAttendance(@PathVariable Long meetingId, @Valid @RequestBody RecordAttendanceDTO recordAttendanceDto) {
        List<AttendanceResponse> attendanceList = attendanceService.recordAttendanceForMeeting(meetingId, recordAttendanceDto);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/meetings/{meetingId}/attendance")
    @PreAuthorize("hasAuthority('DEANERY_STAFF') or (hasAuthority('TEACHER') and @meetingSecurityService.isTeacherForMeeting(#meetingId))")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForMeeting(@PathVariable Long meetingId) {
        List<AttendanceResponse> attendanceList = attendanceService.getAttendanceByMeetingId(meetingId);
        return ResponseEntity.ok(attendanceList);
    }

    @PutMapping("/attendance/{attendanceId}")
    @PreAuthorize("hasAuthority('TEACHER') and @attendanceSecurityService.canTeacherUpdateAttendance(#attendanceId)")
    public ResponseEntity<AttendanceResponse> updateSingleAttendance(@PathVariable Long attendanceId, @Valid @RequestBody UpdateAttendanceDTO updateAttendanceDto) {
        AttendanceResponse updatedAttendance = attendanceService.updateSingleAttendance(attendanceId, updateAttendanceDto);
        return ResponseEntity.ok(updatedAttendance);
    }

}