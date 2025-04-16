package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse { //TODO
    private Long attendanceId;
    private String status;
    private MeetingMinimalResponse meeting;
    private StudentMinimalResponse student;
    private TeacherMinimalResponse recordedByTeacher;
}
