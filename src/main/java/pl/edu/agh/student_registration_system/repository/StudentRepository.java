package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByIndexNumber(String indexNumber);
}
