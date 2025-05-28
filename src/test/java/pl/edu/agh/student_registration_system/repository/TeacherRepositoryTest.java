package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.model.User;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TeacherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeacherRepository teacherRepository;

    private User userT1, userT2, userNonTeacher;
    private Teacher teacher1, teacher2;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);


        userT1 = new User(null, "Teacher", "T1", "pass", "t1@test.com", true, teacherRole, null, null);
        entityManager.persist(userT1);
        teacher1 = new Teacher(null, "Prof.", userT1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        teacherRepository.save(teacher1);

        userT2 = new User(null, "Teacher", "T2", "pass", "t2@test.com", true, teacherRole, null, null);
        entityManager.persist(userT2);
        teacher2 = new Teacher(null, "Dr.", userT2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        teacherRepository.save(teacher2);

        userNonTeacher = new User(null, "NonTeacher", "NT", "pass", "nt@test.com", true, studentRole, null, null);
        entityManager.persist(userNonTeacher);

        entityManager.flush();
    }

    @Test
    void testFindByUser() {
        Optional<Teacher> foundTeacher1 = teacherRepository.findByUser(userT1);
        assertThat(foundTeacher1).isPresent();
        assertThat(foundTeacher1.get()).isEqualTo(teacher1);

        Optional<Teacher> notFoundTeacher = teacherRepository.findByUser(userNonTeacher);
        assertThat(notFoundTeacher).isNotPresent();
    }

    @Test
    void testFindAllWithSpecificationAndPageable() {
        Specification<Teacher> specProf = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("title"), "Prof.");
        Pageable pageable = PageRequest.of(0, 5);
        Page<Teacher> profTeachers = teacherRepository.findAll(specProf, pageable);

        assertThat(profTeachers.getTotalElements()).isEqualTo(1);
        assertThat(profTeachers.getContent()).hasSize(1);
        assertThat(profTeachers.getContent().get(0)).isEqualTo(teacher1);

        Specification<Teacher> specAll = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        Page<Teacher> allTeachers = teacherRepository.findAll(specAll, PageRequest.of(0,1));
        assertThat(allTeachers.getTotalElements()).isEqualTo(2);
        assertThat(allTeachers.getContent()).hasSize(1);
    }
}