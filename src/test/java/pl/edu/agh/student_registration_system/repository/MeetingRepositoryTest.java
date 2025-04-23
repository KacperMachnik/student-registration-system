package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MeetingRepositoryTest {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseGroupRepository courseGroupRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Course course;
    private CourseGroup group1;
    private CourseGroup group2;
    private Meeting meeting1;
    private Meeting meeting2;
    private Meeting meeting3;
    private Meeting meeting4;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        teacherRole = new Role();
        teacherRole.setRoleName(RoleType.TEACHER);
        roleRepository.save(teacherRole);

        User teacherUser = new User();
        teacherUser.setEmail("teacher@example.com");
        teacherUser.setPassword("password");
        teacherUser.setFirstName("Jan");
        teacherUser.setLastName("Profesor");
        teacherUser.setRole(teacherRole);
        teacherUser.setIsActive(true);
        userRepository.save(teacherUser);

        Teacher teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setTitle("Prof.");
        teacherRepository.save(teacher);

        course = new Course();
        course.setCourseName("Programowanie");
        course.setCourseCode("PRG101");
        course.setDescription("Kurs programowania");
        course.setCredits(5);
        courseRepository.save(course);

        group1 = new CourseGroup();
        group1.setCourse(course);
        group1.setTeacher(teacher);
        group1.setGroupNumber(101);
        group1.setMaxCapacity(30);
        courseGroupRepository.save(group1);

        group2 = new CourseGroup();
        group2.setCourse(course);
        group2.setTeacher(teacher);
        group2.setGroupNumber(102);
        group2.setMaxCapacity(25);
        courseGroupRepository.save(group2);

        meeting1 = new Meeting();
        meeting1.setGroup(group1);
        meeting1.setMeetingNumber(1);
        meeting1.setMeetingDate(LocalDateTime.now().plusDays(1));
        meeting1.setTopic("Introduction");
        meetingRepository.save(meeting1);

        meeting2 = new Meeting();
        meeting2.setGroup(group1);
        meeting2.setMeetingNumber(2);
        meeting2.setMeetingDate(LocalDateTime.now().plusDays(8));
        meeting2.setTopic("Basic concepts");
        meetingRepository.save(meeting2);

        meeting3 = new Meeting();
        meeting3.setGroup(group1);
        meeting3.setMeetingNumber(3);
        meeting3.setMeetingDate(LocalDateTime.now().plusDays(15));
        meeting3.setTopic("Advanced topics");
        meetingRepository.save(meeting3);

        meeting4 = new Meeting();
        meeting4.setGroup(group2);
        meeting4.setMeetingNumber(1);
        meeting4.setMeetingDate(LocalDateTime.now().plusDays(2));
        meeting4.setTopic("Introduction for group 2");
        meetingRepository.save(meeting4);
    }

    @Test
    @DisplayName("Should find top meeting by group ordered by meeting number desc")
    void shouldFindTopByGroupOrderByMeetingNumberDesc() {
        Optional<Meeting> latestMeeting = meetingRepository.findTopByGroupOrderByMeetingNumberDesc(group1);

        assertTrue(latestMeeting.isPresent());
        assertEquals(meeting3, latestMeeting.get());
        assertEquals(3, latestMeeting.get().getMeetingNumber());
    }

    @Test
    @DisplayName("Should find meetings by group ordered by meeting number")
    void shouldFindByGroupOrderByMeetingNumber() {
        List<Meeting> meetings = meetingRepository.findByGroupOrderByMeetingNumber(group1);

        assertEquals(3, meetings.size());
        assertEquals(meeting1, meetings.get(0));
        assertEquals(meeting2, meetings.get(1));
        assertEquals(meeting3, meetings.get(2));
    }

    @Test
    @DisplayName("Should save new meeting")
    void shouldSaveNewMeeting() {
        Meeting newMeeting = new Meeting();
        newMeeting.setGroup(group2);
        newMeeting.setMeetingNumber(2);
        newMeeting.setMeetingDate(LocalDateTime.now().plusDays(9));
        newMeeting.setTopic("Second meeting for group 2");

        Meeting savedMeeting = meetingRepository.save(newMeeting);

        assertNotNull(savedMeeting.getMeetingId());

        Optional<Meeting> foundMeeting = meetingRepository.findById(savedMeeting.getMeetingId());
        assertTrue(foundMeeting.isPresent());
        assertEquals(2, foundMeeting.get().getMeetingNumber());
        assertEquals("Second meeting for group 2", foundMeeting.get().getTopic());
    }

    @Test
    @DisplayName("Should update existing meeting")
    void shouldUpdateExistingMeeting() {
        meeting1.setTopic("Updated introduction");
        meeting1.setMeetingDate(LocalDateTime.now().plusDays(2));

        Meeting updatedMeeting = meetingRepository.save(meeting1);

        assertEquals(meeting1.getMeetingId(), updatedMeeting.getMeetingId());
        assertEquals("Updated introduction", updatedMeeting.getTopic());

        Optional<Meeting> foundMeeting = meetingRepository.findById(meeting1.getMeetingId());
        assertTrue(foundMeeting.isPresent());
        assertEquals("Updated introduction", foundMeeting.get().getTopic());
    }

    @Test
    @DisplayName("Should delete meeting")
    void shouldDeleteMeeting() {
        meetingRepository.delete(meeting4);

        Optional<Meeting> foundMeeting = meetingRepository.findById(meeting4.getMeetingId());
        assertFalse(foundMeeting.isPresent());
    }
}
