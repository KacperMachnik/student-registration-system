package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Role studentRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        entityManager.flush();
    }

    @Test
    void testFindByRoleName() {
        Optional<Role> foundStudentRole = roleRepository.findByRoleName(RoleType.STUDENT);
        assertThat(foundStudentRole).isPresent();
        assertThat(foundStudentRole.get().getRoleName()).isEqualTo(RoleType.STUDENT);
        assertThat(foundStudentRole.get().getRoleId()).isEqualTo(studentRole.getRoleId());

        Optional<Role> foundTeacherRole = roleRepository.findByRoleName(RoleType.TEACHER);
        assertThat(foundTeacherRole).isPresent();
        assertThat(foundTeacherRole.get().getRoleName()).isEqualTo(RoleType.TEACHER);

        Optional<Role> notFoundRole = roleRepository.findByRoleName(RoleType.DEANERY_STAFF);
        assertThat(notFoundRole).isNotPresent();
    }
}