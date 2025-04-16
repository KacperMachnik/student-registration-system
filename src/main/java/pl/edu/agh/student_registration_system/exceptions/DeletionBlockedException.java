package pl.edu.agh.student_registration_system.exceptions;

public class DeletionBlockedException extends RuntimeException {
    public DeletionBlockedException(String message) {
        super(message);
    }
}
