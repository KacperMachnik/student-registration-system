package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseDTO {
    @NotBlank
    @Size(max = 50)
    private String courseCode;
    @NotBlank
    @Size(max = 255)
    private String courseName;
    @Size(max = 2000)
    private String description;
    private Integer credits;
}