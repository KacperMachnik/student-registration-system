package pl.edu.agh.student_registration_system.model;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class TeacherModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Role teacherRole;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);

        testUser = new User(null, "Severus", "Snape", "password", "s.snape@example.com", true, teacherRole, null, null);
        entityManager.persist(testUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindTeacher() {
        Teacher teacher = new Teacher(null, "Prof.", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        Teacher savedTeacher = entityManager.persistAndFlush(teacher);

        assertThat(savedTeacher).isNotNull();
        assertThat(savedTeacher.getTeacherId()).isNotNull();
        assertThat(savedTeacher.getTitle()).isEqualTo("Prof.");
        assertThat(savedTeacher.getUser().getUserId()).isEqualTo(testUser.getUserId());

        Teacher foundTeacher = entityManager.find(Teacher.class, savedTeacher.getTeacherId());
        assertThat(foundTeacher).isEqualTo(savedTeacher);
        assertThat(foundTeacher.getUser().getFirstName()).isEqualTo("Severus");
    }

    @Test
    void testUserIsNotNull() {
        Teacher teacher = new Teacher(null, "Dr.", null, new HashSet<>(), new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(teacher);
        });
    }

    @Test
    void testOneToManyTaughtGroupsRelationship() {
        Teacher teacher = new Teacher(null, "Prof.", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);

        Course course = new Course(null, "Potions", "POT101", "Brewing potions", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);

        CourseGroup group1 = new CourseGroup(null, 1, 20, course, teacher, new HashSet<>(), new ArrayList<>());
        CourseGroup group2 = new CourseGroup(null, 2, 25, course, teacher, new HashSet<>(), new ArrayList<>());

        teacher.getTaughtGroups().add(group1);
        teacher.getTaughtGroups().add(group2);

        entityManager.persistAndFlush(group1);
        entityManager.persistAndFlush(group2);
        entityManager.clear();

        Teacher foundTeacher = entityManager.find(Teacher.class, teacher.getTeacherId());
        assertThat(foundTeacher.getTaughtGroups()).hasSize(2);
        assertThat(foundTeacher.getTaughtGroups().stream().map(CourseGroup::getGroupNumber)).contains(1, 2);
    }

    @Test
    void testOneToManyIssuedGradesRelationship() {
        Teacher teacher = new Teacher(null, "Prof.", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);

        User studentUser = new User(null, "Harry", "Potter", "pass", "h.potter@example.com", true, studentRole, null, null);
        entityManager.persist(studentUser);
        Student student = new Student(null, "445566", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        Course course = new Course(null, "Defense Against Dark Arts", "DADA202", "Spells", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);

        Grade grade = new Grade(null, "E", java.time.LocalDateTime.now(), "Needs improvement", student, course, teacher);
        teacher.getIssuedGrades().add(grade);
        entityManager.persistAndFlush(grade);
        entityManager.clear();

        Teacher foundTeacher = entityManager.find(Teacher.class, teacher.getTeacherId());
        assertThat(foundTeacher.getIssuedGrades()).hasSize(1);
        assertThat(foundTeacher.getIssuedGrades().iterator().next().getGradeValue()).isEqualTo("E");
    }

    @Test
    void testOneToManyRecordedAttendancesRelationship() {
        Teacher teacher = new Teacher(null, "Prof.", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);

        User studentUser = new User(null, "Hermione", "Granger", "pass", "h.granger@example.com", true, studentRole, null, null);
        entityManager.persist(studentUser);
        Student student = new Student(null, "778899", studentUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        Course course = new Course(null, "Charms", "CHARM101", "Charms", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);
        CourseGroup group = new CourseGroup(null, 1, 10, course, teacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group);
        Meeting meeting = new Meeting(null, 1, java.time.LocalDateTime.now(), "Lecture 1", group, new HashSet<>());
        entityManager.persist(meeting);

        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, meeting, student, teacher);
        teacher.getRecordedAttendances().add(attendance);
        entityManager.persistAndFlush(attendance);
        entityManager.clear();

        Teacher foundTeacher = entityManager.find(Teacher.class, teacher.getTeacherId());
        assertThat(foundTeacher.getRecordedAttendances()).hasSize(1);
        assertThat(foundTeacher.getRecordedAttendances().iterator().next().getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }
}