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
class StudentModelTest {

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Role studentRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleType.STUDENT);
        entityManager.persist(studentRole);

        teacherRole = new Role(RoleType.TEACHER);
        entityManager.persist(teacherRole);

        testUser = new User(null, "Rick", "Sanchez", "password", "rick.s@example.com", true, studentRole, null, null);
        entityManager.persist(testUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndFindStudent() {
        Student student = new Student(null, "987654", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        Student savedStudent = entityManager.persistAndFlush(student);

        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getStudentId()).isNotNull();
        assertThat(savedStudent.getIndexNumber()).isEqualTo("987654");
        assertThat(savedStudent.getUser().getUserId()).isEqualTo(testUser.getUserId());

        Student foundStudent = entityManager.find(Student.class, savedStudent.getStudentId());
        assertThat(foundStudent).isEqualTo(savedStudent);
        assertThat(foundStudent.getUser().getFirstName()).isEqualTo("Rick");
    }

    @Test
    void testIndexNumberUniqueConstraint() {
        Student student1 = new Student(null, "112233", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persistAndFlush(student1);

        User user2 = new User(null, "Morty", "Smith", "pass", "morty.s@example.com", true, studentRole, null, null);
        entityManager.persist(user2);
        Student student2 = new Student(null, "112233", user2, new HashSet<>(), new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(student2);
        });
    }

    @Test
    void testIndexNumberIsNotNull() {
        Student student = new Student(null, null, testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(student);
        });
    }

    @Test
    void testUserIsNotNull() {
        Student student = new Student(null, "123456", null, new HashSet<>(), new HashSet<>(), new HashSet<>());
        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persistAndFlush(student);
        });
    }

    @Test
    void testOneToManyEnrollmentsRelationship() {
        Student student = new Student(null, "111111", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        Course course = new Course(null, "Space Travel", "SP101", "Intro", 3, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);
        User teacherUser = new User(null, "Summer", "Smith", "pass", "summer.s@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        Teacher teacher = new Teacher(null, "Ms.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);
        CourseGroup group = new CourseGroup(null, 1, 10, course, teacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group);

        Enrollment enrollment = new Enrollment(null, java.time.LocalDateTime.now(), student, group);
        student.getEnrollments().add(enrollment);
        entityManager.persistAndFlush(enrollment);
        entityManager.clear();

        Student foundStudent = entityManager.find(Student.class, student.getStudentId());
        assertThat(foundStudent.getEnrollments()).hasSize(1);
        assertThat(foundStudent.getEnrollments().iterator().next().getGroup().getCourseGroupId()).isEqualTo(group.getCourseGroupId());

        foundStudent.getEnrollments().clear();
        entityManager.flush();
        entityManager.clear();
        foundStudent = entityManager.find(Student.class, student.getStudentId());
        assertThat(foundStudent.getEnrollments()).isEmpty();
    }

    @Test
    void testOneToManyGradesRelationship() {
        Student student = new Student(null, "222222", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        Course course = new Course(null, "Alien Anatomy", "AA202", "Study", 4, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);
        User teacherUser = new User(null, "Beth", "Smith", "pass", "beth.s@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        Teacher teacher = new Teacher(null, "Dr.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);

        Grade grade = new Grade(null, "A+", java.time.LocalDateTime.now(), "Fantastic!", student, course, teacher);
        student.getGrades().add(grade);
        entityManager.persistAndFlush(grade);
        entityManager.clear();

        Student foundStudent = entityManager.find(Student.class, student.getStudentId());
        assertThat(foundStudent.getGrades()).hasSize(1);
        assertThat(foundStudent.getGrades().iterator().next().getGradeValue()).isEqualTo("A+");

        foundStudent.getGrades().clear();
        entityManager.flush();
        entityManager.clear();
        foundStudent = entityManager.find(Student.class, student.getStudentId());
        assertThat(foundStudent.getGrades()).isEmpty();
    }

    @Test
    void testOneToManyAttendanceRecordsRelationship() {
        Student student = new Student(null, "333333", testUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(student);

        Course course = new Course(null, "Interdimensional Math", "IM303", "Math", 5, new HashSet<>(), new HashSet<>());
        entityManager.persist(course);
        User teacherUser = new User(null, "Jerry", "Smith", "pass", "jerry.s@example.com", true, teacherRole, null, null);
        entityManager.persist(teacherUser);
        Teacher teacher = new Teacher(null, "Mr.", teacherUser, new HashSet<>(), new HashSet<>(), new HashSet<>());
        entityManager.persist(teacher);
        CourseGroup group = new CourseGroup(null, 1, 15, course, teacher, new HashSet<>(), new ArrayList<>());
        entityManager.persist(group);
        Meeting meeting = new Meeting(null, 1, java.time.LocalDateTime.now(), "Intro", group, new HashSet<>());
        entityManager.persist(meeting);

        Attendance attendance = new Attendance(null, AttendanceStatus.PRESENT, meeting, student, teacher);
        student.getAttendanceRecords().add(attendance);
        entityManager.persistAndFlush(attendance);
        entityManager.clear();

        Student foundStudent = entityManager.find(Student.class, student.getStudentId());
        assertThat(foundStudent.getAttendanceRecords()).hasSize(1);
        assertThat(foundStudent.getAttendanceRecords().iterator().next().getStatus()).isEqualTo(AttendanceStatus.PRESENT);

        foundStudent.getAttendanceRecords().clear();
        entityManager.flush();
        entityManager.clear();
        foundStudent = entityManager.find(Student.class, student.getStudentId());
        assertThat(foundStudent.getAttendanceRecords()).isEmpty();
    }
}