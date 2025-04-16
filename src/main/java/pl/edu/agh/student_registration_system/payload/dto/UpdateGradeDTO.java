package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGradeDTO { // Dla Endpointu 39
    @NotBlank
    private String gradeValue;
    @Size(max = 1000)
    private String comment;
}