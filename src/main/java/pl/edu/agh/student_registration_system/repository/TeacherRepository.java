package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
