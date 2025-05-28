package pl.edu.agh.student_registration_system.model;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class RoleModelTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testPersistAndFindRole() {
        Role role = new Role(null, RoleType.STUDENT, new HashSet<>());
        Role savedRole = entityManager.persistAndFlush(role);

        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getRoleId()).isNotNull();
        assertThat(savedRole.getRoleName()).isEqualTo(RoleType.STUDENT);

        Role foundRole = entityManager.find(Role.class, savedRole.getRoleId());
        assertThat(foundRole).isEqualTo(savedRole);
    }

    @Test
    void testRoleNameUniqueConstraint() {
        Role role1 = new Role(null, RoleType.TEACHER, new HashSet<>());
        entityManager.persistAndFlush(role1);

        Role role2 = new Role(null, RoleType.TEACHER, new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(role2);
        });
    }

    @Test
    void testRoleNameIsNotNull() {
        Role role = new Role(null, null, new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(role);
        });
    }

    @Test
    void testOneToManyUsersRelationship() {
        Role role = new Role(null, RoleType.DEANERY_STAFF, new HashSet<>());
        entityManager.persist(role);

        User user1 = new User(null, "Dean", "Smith", "pass1", "dean.s@example.com", true, role, null, null);
        User user2 = new User(null, "Assistant", "Jones", "pass2", "ass.j@example.com", true, role, null, null);

        role.getUsers().add(user1);
        role.getUsers().add(user2);

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.clear();

        Role foundRole = entityManager.find(Role.class, role.getRoleId());
        assertThat(foundRole.getUsers()).hasSize(2);
        assertThat(foundRole.getUsers().stream().map(User::getFirstName)).contains("Dean", "Assistant");
    }
}