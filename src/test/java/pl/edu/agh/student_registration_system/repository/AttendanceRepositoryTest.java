package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AttendanceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private Student student1;
    private Meeting meeting1;
    private Teacher teacher1;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);
        Role teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        User userS1 = new User(null, "Student", "One", "pass", "s1@example.com", true, studentRole, null, null);
        entityManager.persist(userS1);
        student1 = new Student(null, "100001", userS1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student1);

        User userT1 = new User(null, "Teacher", "One", "pass", "t1@example.com", true, teacherRole, null, null);
        entityManager.persist(userT1);
        teacher1 = new Teacher(null, "Dr.", userT1, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher1);

        Course course1 = new Course(null, "Course 1", "C1", "Desc 1", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course1);

        CourseGroup group1 = new CourseGroup(null, 1, 20, course1, teacher1, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group1);

        meeting1 = new Meeting(null, 1, LocalDateTime.now(), "Meeting 1", group1, new HashSet<>());
        entityManager.persist(meeting1);

        entityManager.flush();
    }

    @Test
    void testFindAllWithSpecification() {
        Attendance attendance1 = new Attendance(null, AttendanceStatus.PRESENT, meeting1, student1, teacher1);
        attendanceRepository.save(attendance1);

        User userS2 = new User(null, "Student", "Two", "pass", "s2@example.com", true, entityManager.find(Role.class, student1.getUser().getRole().getRoleId()), null, null);
        entityManager.persist(userS2);
        Student student2 = new Student(null, "100002", userS2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student2);
        Attendance attendance2 = new Attendance(null, AttendanceStatus.ABSENT, meeting1, student2, teacher1);
        attendanceRepository.save(attendance2);

        Specification<Attendance> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), AttendanceStatus.PRESENT);

        List<Attendance> presentAttendances = attendanceRepository.findAll(spec);
        assertThat(presentAttendances).hasSize(1);
        assertThat(presentAttendances.get(0).getStudent()).isEqualTo(student1);

        Specification<Attendance> specAbsent = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), AttendanceStatus.ABSENT);
        List<Attendance> absentAttendances = attendanceRepository.findAll(specAbsent);
        assertThat(absentAttendances).hasSize(1);
        assertThat(absentAttendances.get(0).getStudent()).isEqualTo(student2);
    }

    @Test
    void testFindByMeetingAndStudent() {
        Attendance attendance = new Attendance(null, AttendanceStatus.EXCUSED, meeting1, student1, teacher1);
        attendanceRepository.save(attendance);

        Optional<Attendance> foundAttendance = attendanceRepository.findByMeetingAndStudent(meeting1, student1);
        assertThat(foundAttendance).isPresent();
        assertThat(foundAttendance.get().getStatus()).isEqualTo(AttendanceStatus.EXCUSED);

        User userS2 = new User(null, "Student", "Three", "pass", "s3@example.com", true, entityManager.find(Role.class, student1.getUser().getRole().getRoleId()), null, null);
        entityManager.persist(userS2);
        Student studentNonExistent = new Student(null, "100003", userS2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(studentNonExistent);
        Optional<Attendance> notFoundAttendance = attendanceRepository.findByMeetingAndStudent(meeting1, studentNonExistent);
        assertThat(notFoundAttendance).isNotPresent();
    }
}