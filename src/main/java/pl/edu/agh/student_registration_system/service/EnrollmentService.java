package pl.edu.agh.student_registration_system.service;

import jakarta.validation.constraints.NotNull;
import pl.edu.agh.student_registration_system.payload.response.EnrollmentResponse;

public interface EnrollmentService {
    EnrollmentResponse enrollCurrentUser(@NotNull Long groupId);

    void unenrollCurrentUser(Long groupId);

    EnrollmentResponse enrollStudentById(Long studentId, Long groupId, boolean b);

    void unenrollStudentById(Long studentId, Long groupId);
}
