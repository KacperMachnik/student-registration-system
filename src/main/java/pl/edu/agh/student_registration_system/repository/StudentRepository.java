package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Optional;


public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    boolean existsByIndexNumber(String indexNumber);

    Optional<Student> findByUser(User currentUser);


    @Query("SELECT s FROM students s JOIN FETCH s.user u JOIN FETCH u.role WHERE s.studentId = :id")
    Optional<Student> findByIdWithUser(@Param("id") Long id);
}
