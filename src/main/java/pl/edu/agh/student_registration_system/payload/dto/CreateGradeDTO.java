package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGradeDTO {
    @NotNull
    private Long studentId;
    @NotNull
    private Long courseId;
    @NotBlank
    private String gradeValue;
    @Size(max = 1000)
    private String comment;
}