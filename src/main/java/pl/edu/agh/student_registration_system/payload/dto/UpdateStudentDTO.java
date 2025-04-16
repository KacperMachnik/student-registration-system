package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudentDTO {
    @Size(max = 80)
    @Email
    private String email;

    @Size(max = 50)
    private String firstName;
    @Size(max = 50)
    private String lastName;

    private Boolean isActive;
}
