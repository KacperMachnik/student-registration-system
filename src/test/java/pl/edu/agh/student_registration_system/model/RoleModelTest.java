package pl.edu.agh.student_registration_system.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RoleModelTest {

    @Test
    void shouldCreateRoleWithAllFields() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName(RoleType.STUDENT);

        assertEquals(1L, role.getRoleId());
        assertEquals(RoleType.STUDENT, role.getRoleName());
        assertNotNull(role.getUsers());
    }

    @Test
    void shouldCreateRoleWithConstructor() {
        Role role = new Role(1L, RoleType.TEACHER, new HashSet<>());

        assertEquals(1L, role.getRoleId());
        assertEquals(RoleType.TEACHER, role.getRoleName());
        assertNotNull(role.getUsers());
    }

    @Test
    void shouldCreateRoleWithRoleTypeConstructor() {
        Role role = new Role(RoleType.DEANERY_STAFF);

        assertEquals(RoleType.DEANERY_STAFF, role.getRoleName());
        assertNotNull(role.getUsers());
    }

    @Test
    void shouldAddUser() {
        Role role = new Role();
        User user = new User();
        user.setRole(role);

        role.getUsers().add(user);

        assertEquals(1, role.getUsers().size());
        assertTrue(role.getUsers().contains(user));
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Role role1 = new Role();
        role1.setRoleId(1L);

        Role role2 = new Role();
        role2.setRoleId(1L);

        Role role3 = new Role();
        role3.setRoleId(2L);

        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    void shouldImplementToString() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName(RoleType.STUDENT);

        String toString = role.toString();

        assertTrue(toString.contains("roleId=1"));
        assertTrue(toString.contains("roleName=STUDENT"));
    }
}
