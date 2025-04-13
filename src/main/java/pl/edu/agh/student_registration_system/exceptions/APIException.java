package pl.edu.agh.student_registration_system.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class APIException extends RuntimeException {
    public APIException(String message) {
        super(message);
    }
}
