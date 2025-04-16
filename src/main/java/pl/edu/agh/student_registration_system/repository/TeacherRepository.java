package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User currentUser);

    Page<Teacher> findAll(Specification<Teacher> spec, Pageable pageable);
}
