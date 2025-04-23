package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceModelTest {

    @Test
    void shouldCreateAttendanceWithAllFields() {
        Meeting meeting = new Meeting();
        Student student = new Student();
        Teacher teacher = new Teacher();

        Attendance attendance = new Attendance();
        attendance.setAttendanceId(1L);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setMeeting(meeting);
        attendance.setStudent(student);
        attendance.setRecordedByTeacher(teacher);

        assertEquals(1L, attendance.getAttendanceId());
        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
        assertEquals(meeting, attendance.getMeeting());
        assertEquals(student, attendance.getStudent());
        assertEquals(teacher, attendance.getRecordedByTeacher());
    }

    @Test
    void shouldCreateAttendanceWithConstructor() {
        Meeting meeting = new Meeting();
        Student student = new Student();
        Teacher teacher = new Teacher();

        Attendance attendance = new Attendance(1L, AttendanceStatus.ABSENT, meeting, student, teacher);

        assertEquals(1L, attendance.getAttendanceId());
        assertEquals(AttendanceStatus.ABSENT, attendance.getStatus());
        assertEquals(meeting, attendance.getMeeting());
        assertEquals(student, attendance.getStudent());
        assertEquals(teacher, attendance.getRecordedByTeacher());
    }

    @Test
    void shouldUpdateAttendanceStatus() {
        Attendance attendance = new Attendance();
        attendance.setStatus(AttendanceStatus.PRESENT);

        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());

        attendance.setStatus(AttendanceStatus.EXCUSED);
        assertEquals(AttendanceStatus.EXCUSED, attendance.getStatus());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Attendance attendance1 = new Attendance();
        attendance1.setAttendanceId(1L);

        Attendance attendance2 = new Attendance();
        attendance2.setAttendanceId(1L);

        Attendance attendance3 = new Attendance();
        attendance3.setAttendanceId(2L);

        assertEquals(attendance1, attendance2);
        assertNotEquals(attendance1, attendance3);
        assertEquals(attendance1.hashCode(), attendance2.hashCode());
        assertNotEquals(attendance1.hashCode(), attendance3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(1L);
        attendance.setStatus(AttendanceStatus.PRESENT);

        String toString = attendance.toString();

        assertTrue(toString.contains("attendanceId=1"));
        assertTrue(toString.contains("status=PRESENT"));
    }
}
