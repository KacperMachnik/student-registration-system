package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse { // Odpowiedź dla zapisu
    private Long enrollmentId;
    private Long studentId;
    private Long groupId;
    private String enrollmentDate; // Jako String ISO
}