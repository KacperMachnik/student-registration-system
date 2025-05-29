package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MeetingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MeetingRepository meetingRepository;

    private CourseGroup group1;
    private Meeting meeting1, meeting2, meeting3;

    @BeforeEach
    void setUp() {
        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        User userT1 = new User(null, "Teacher", "Met", "pass", "tmet@example.com", true, teacherRole, null, null);
        entityManager.persist(userT1);
        Teacher teacher1 = new Teacher(null, "Prof.", userT1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher1);

        Course course1 = new Course(null, "Course M", "CM1", "Desc M", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course1);

        group1 = new CourseGroup(null, 1, 20, course1, teacher1, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group1);

        meeting1 = new Meeting(null, 1, LocalDateTime.now().minusDays(2), "Intro", group1, new HashSet<>());
        meetingRepository.save(meeting1);
        meeting2 = new Meeting(null, 2, LocalDateTime.now().minusDays(1), "Mid", group1, new HashSet<>());
        meetingRepository.save(meeting2);
        meeting3 = new Meeting(null, 3, LocalDateTime.now(), "Final", group1, new HashSet<>());
        meetingRepository.save(meeting3);

        entityManager.flush();
    }

    @Test
    void testFindTopByGroupOrderByMeetingNumberDesc() {
        Optional<Meeting> topMeeting = meetingRepository.findTopByGroupOrderByMeetingNumberDesc(group1);
        assertThat(topMeeting).isPresent();
        assertThat(topMeeting.get()).isEqualTo(meeting3);
        assertThat(topMeeting.get().getMeetingNumber()).isEqualTo(3);
    }

    @Test
    void testFindByGroupOrderByMeetingNumber() {
        List<Meeting> meetings = meetingRepository.findByGroupOrderByMeetingNumber(group1);
        assertThat(meetings).hasSize(3);
        assertThat(meetings).containsExactly(meeting1, meeting2, meeting3);
        assertThat(meetings.get(0).getMeetingNumber()).isEqualTo(1);
        assertThat(meetings.get(1).getMeetingNumber()).isEqualTo(2);
        assertThat(meetings.get(2).getMeetingNumber()).isEqualTo(3);
    }
}