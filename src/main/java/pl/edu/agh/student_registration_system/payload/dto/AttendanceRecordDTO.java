package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDTO {
    @NotNull
    private Long studentId;
    @NotBlank
    private String status;
}