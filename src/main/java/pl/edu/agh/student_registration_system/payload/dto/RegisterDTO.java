package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pl.edu.agh.student_registration_system.model.RoleType;

@Data
public class RegisterDTO {

    @NotBlank
    @Size(min = 6, max = 120)
    private String password;

    @NotBlank
    @Size(max = 80)
    @Email
    private String email;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    private RoleType roleType; // STUDENT lub TEACHER lub DEANERY_STAFF

    @Size(max = 100)
    private String title; // TEACHER only
}