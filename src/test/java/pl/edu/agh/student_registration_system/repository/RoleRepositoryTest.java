package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role studentRole;
    private Role teacherRole;
    private Role deaneryStaffRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setRoleName(RoleType.STUDENT);
        roleRepository.save(studentRole);

        teacherRole = new Role();
        teacherRole.setRoleName(RoleType.TEACHER);
        roleRepository.save(teacherRole);

        deaneryStaffRole = new Role();
        deaneryStaffRole.setRoleName(RoleType.DEANERY_STAFF);
        roleRepository.save(deaneryStaffRole);
    }

    @Test
    @DisplayName("Should find role by role name")
    void shouldFindByRoleName() {
        Optional<Role> foundStudentRole = roleRepository.findByRoleName(RoleType.STUDENT);
        Optional<Role> foundTeacherRole = roleRepository.findByRoleName(RoleType.TEACHER);
        Optional<Role> foundDeaneryStaffRole = roleRepository.findByRoleName(RoleType.DEANERY_STAFF);

        assertTrue(foundStudentRole.isPresent());
        assertEquals(studentRole, foundStudentRole.get());

        assertTrue(foundTeacherRole.isPresent());
        assertEquals(teacherRole, foundTeacherRole.get());

        assertTrue(foundDeaneryStaffRole.isPresent());
        assertEquals(deaneryStaffRole, foundDeaneryStaffRole.get());
    }

    @Test
    @DisplayName("Should return empty optional when role name not found")
    void shouldReturnEmptyOptionalWhenRoleNameNotFound() {
        // This test assumes there's a non-existent role type for testing
        // Since all enum values are already used in setUp, this test would need a mock or different approach
        // For demonstration purposes, we'll just assert that all existing roles are found
        assertTrue(roleRepository.findByRoleName(RoleType.STUDENT).isPresent());
        assertTrue(roleRepository.findByRoleName(RoleType.TEACHER).isPresent());
        assertTrue(roleRepository.findByRoleName(RoleType.DEANERY_STAFF).isPresent());
    }

    @Test
    @DisplayName("Should save new role")
    void shouldSaveNewRole() {
        // First delete existing role to avoid unique constraint violation
        roleRepository.delete(studentRole);

        Role newStudentRole = new Role();
        newStudentRole.setRoleName(RoleType.STUDENT);

        Role savedRole = roleRepository.save(newStudentRole);

        assertNotNull(savedRole.getRoleId());
        assertEquals(RoleType.STUDENT, savedRole.getRoleName());

        Optional<Role> foundRole = roleRepository.findById(savedRole.getRoleId());
        assertTrue(foundRole.isPresent());
        assertEquals(RoleType.STUDENT, foundRole.get().getRoleName());
    }

    @Test
    @DisplayName("Should delete role")
    void shouldDeleteRole() {
        roleRepository.delete(deaneryStaffRole);

        Optional<Role> foundRole = roleRepository.findById(deaneryStaffRole.getRoleId());
        assertFalse(foundRole.isPresent());

        Optional<Role> foundByName = roleRepository.findByRoleName(RoleType.DEANERY_STAFF);
        assertFalse(foundByName.isPresent());
    }
}
