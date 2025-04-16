package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Meeting;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findTopByGroupOrderByMeetingNumberDesc(CourseGroup group);

    List<Meeting> findByGroupOrderByMeetingNumber(CourseGroup group);
}
