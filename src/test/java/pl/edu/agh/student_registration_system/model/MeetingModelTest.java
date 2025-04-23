package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class MeetingModelTest {

    @Test
    void shouldCreateMeetingWithAllFields() {
        CourseGroup group = new CourseGroup();
        LocalDateTime meetingDate = LocalDateTime.now();

        Meeting meeting = new Meeting();
        meeting.setMeetingId(1L);
        meeting.setMeetingNumber(1);
        meeting.setMeetingDate(meetingDate);
        meeting.setTopic("Introduction");
        meeting.setGroup(group);

        assertEquals(1L, meeting.getMeetingId());
        assertEquals(1, meeting.getMeetingNumber());
        assertEquals(meetingDate, meeting.getMeetingDate());
        assertEquals("Introduction", meeting.getTopic());
        assertEquals(group, meeting.getGroup());
        assertNotNull(meeting.getAttendanceRecords());
    }

    @Test
    void shouldCreateMeetingWithConstructor() {
        CourseGroup group = new CourseGroup();
        LocalDateTime meetingDate = LocalDateTime.now();

        Meeting meeting = new Meeting(1L, 1, meetingDate, "Introduction", group, new HashSet<>());

        assertEquals(1L, meeting.getMeetingId());
        assertEquals(1, meeting.getMeetingNumber());
        assertEquals(meetingDate, meeting.getMeetingDate());
        assertEquals("Introduction", meeting.getTopic());
        assertEquals(group, meeting.getGroup());
        assertNotNull(meeting.getAttendanceRecords());
    }

    @Test
    void shouldAddAttendanceRecord() {
        Meeting meeting = new Meeting();
        Attendance attendance = new Attendance();
        attendance.setMeeting(meeting);

        meeting.getAttendanceRecords().add(attendance);

        assertEquals(1, meeting.getAttendanceRecords().size());
        assertTrue(meeting.getAttendanceRecords().contains(attendance));
    }

    @Test
    void shouldUpdateTopic() {
        Meeting meeting = new Meeting();
        meeting.setTopic("Initial topic");

        assertEquals("Initial topic", meeting.getTopic());

        meeting.setTopic("Updated topic");
        assertEquals("Updated topic", meeting.getTopic());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Meeting meeting1 = new Meeting();
        meeting1.setMeetingId(1L);

        Meeting meeting2 = new Meeting();
        meeting2.setMeetingId(1L);

        Meeting meeting3 = new Meeting();
        meeting3.setMeetingId(2L);

        assertEquals(meeting1, meeting2);
        assertNotEquals(meeting1, meeting3);
        assertEquals(meeting1.hashCode(), meeting2.hashCode());
        assertNotEquals(meeting1.hashCode(), meeting3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Meeting meeting = new Meeting();
        meeting.setMeetingId(1L);
        meeting.setMeetingNumber(1);
        meeting.setTopic("Introduction");

        String toString = meeting.toString();

        assertTrue(toString.contains("meetingId=1"));
        assertTrue(toString.contains("meetingNumber=1"));
        assertTrue(toString.contains("topic=Introduction"));
    }
}
