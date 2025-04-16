package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordAttendanceDTO { //TODO
    @NotEmpty
    private List<AttendanceRecordDTO> attendanceList;
}