package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceStatusModelTest {

    @Test
    void shouldHaveThreeValues() {
        assertEquals(3, AttendanceStatus.values().length);
    }

    @Test
    void shouldContainPresentValue() {
        assertTrue(containsEnumValue(AttendanceStatus.class, "PRESENT"));
    }

    @Test
    void shouldContainAbsentValue() {
        assertTrue(containsEnumValue(AttendanceStatus.class, "ABSENT"));
    }

    @Test
    void shouldContainExcusedValue() {
        assertTrue(containsEnumValue(AttendanceStatus.class, "EXCUSED"));
    }

    private <E extends Enum<E>> boolean containsEnumValue(Class<E> enumClass, String value) {
        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
