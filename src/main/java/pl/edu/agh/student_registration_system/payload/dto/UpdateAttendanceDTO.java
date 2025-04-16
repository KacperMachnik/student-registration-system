package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendanceDTO { // Dla Endpointu 45
    @NotBlank
    private String status; // "PRESENT", "ABSENT", "EXCUSED"
}