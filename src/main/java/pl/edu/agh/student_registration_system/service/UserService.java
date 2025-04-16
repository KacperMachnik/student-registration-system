package pl.edu.agh.student_registration_system.service;

import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.payload.dto.RegisterDTO;

public interface UserService {
    User registerNewUser(RegisterDTO request);

    User getCurrentAuthenticatedUser();
}