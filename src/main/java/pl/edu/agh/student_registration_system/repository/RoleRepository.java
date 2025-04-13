package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleType roleType);
}
