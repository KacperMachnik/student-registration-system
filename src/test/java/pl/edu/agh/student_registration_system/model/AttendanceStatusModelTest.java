package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceStatusModelTest {

    @Test
    void testAttendanceStatusEnumValues() {
        assertThat(AttendanceStatus.values()).containsExactlyInAnyOrder(
                AttendanceStatus.PRESENT,
                AttendanceStatus.ABSENT,
                AttendanceStatus.EXCUSED
        );
    }

    @Test
    void testValueOfExistingStatus() {
        assertDoesNotThrow(() -> AttendanceStatus.valueOf("PRESENT"));
        assertThat(AttendanceStatus.valueOf("PRESENT")).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void testValueOfNonExistingStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> AttendanceStatus.valueOf("INVALID_STATUS"));
    }
}