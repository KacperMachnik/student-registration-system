package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTypeModelTest {

    @Test
    void shouldHaveThreeValues() {
        assertEquals(3, RoleType.values().length);
    }

    @Test
    void shouldContainStudentValue() {
        assertTrue(containsEnumValue(RoleType.class, "STUDENT"));
    }

    @Test
    void shouldContainTeacherValue() {
        assertTrue(containsEnumValue(RoleType.class, "TEACHER"));
    }

    @Test
    void shouldContainDeaneryStaffValue() {
        assertTrue(containsEnumValue(RoleType.class, "DEANERY_STAFF"));
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
