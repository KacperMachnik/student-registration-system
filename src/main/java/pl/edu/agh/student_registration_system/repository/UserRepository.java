package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String adminEmail);
}
