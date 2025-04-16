package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.edu.agh.student_registration_system.model.Attendance;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.model.Student;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    List<Attendance> findAll(Specification<Attendance> spec);

    Optional<Attendance> findByMeetingAndStudent(Meeting meeting, Student student);
}
