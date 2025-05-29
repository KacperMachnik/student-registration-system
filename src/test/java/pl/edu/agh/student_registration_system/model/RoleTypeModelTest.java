package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleTypeModelTest {

    @Test
    void testRoleTypeEnumValues() {
        assertThat(RoleType.values()).containsExactlyInAnyOrder(
                RoleType.STUDENT,
                RoleType.TEACHER,
                RoleType.DEANERY_STAFF
        );
    }

    @Test
    void testValueOfExistingRoleType() {
        assertDoesNotThrow(() -> RoleType.valueOf("STUDENT"));
        assertThat(RoleType.valueOf("STUDENT")).isEqualTo(RoleType.STUDENT);
    }

    @Test
    void testValueOfNonExistingRoleTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> RoleType.valueOf("ADMIN"));
    }
}