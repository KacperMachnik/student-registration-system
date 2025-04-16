package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherMinimalResponse {
    private Long teacherId;
    private String firstName;
    private String lastName;
    private String title;
}