package pl.edu.agh.student_registration_system.exceptions;

public class EnrollmentConflictException extends RuntimeException {
    public EnrollmentConflictException(String message) {
        super(message);
    }
}