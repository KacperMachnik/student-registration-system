package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMinimalResponse {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String indexNumber;
}