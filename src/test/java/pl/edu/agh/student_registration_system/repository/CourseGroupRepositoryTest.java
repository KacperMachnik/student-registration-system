package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CourseGroupRepositoryTest {

    @Autowired
    private CourseGroupRepository courseGroupRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Course course1;
    private Course course2;
    private Teacher teacher1;
    private Teacher teacher2;
    private Student student1;
    private Student student2;
    private CourseGroup courseGroup1;
    private CourseGroup courseGroup2;
    private CourseGroup courseGroup3;
    private Role studentRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setRoleName(RoleType.STUDENT);
        roleRepository.save(studentRole);

        teacherRole = new Role();
        teacherRole.setRoleName(RoleType.TEACHER);
        roleRepository.save(teacherRole);

        User teacherUser1 = new User();
        teacherUser1.setEmail("teacher1@example.com");
        teacherUser1.setPassword("password");
        teacherUser1.setFirstName("Jan");
        teacherUser1.setLastName("Profesor");
        teacherUser1.setRole(teacherRole);
        teacherUser1.setIsActive(true);
        userRepository.save(teacherUser1);

        User teacherUser2 = new User();
        teacherUser2.setEmail("teacher2@example.com");
        teacherUser2.setPassword("password");
        teacherUser2.setFirstName("Anna");
        teacherUser2.setLastName("Doktor");
        teacherUser2.setRole(teacherRole);
        teacherUser2.setIsActive(true);
        userRepository.save(teacherUser2);

        User studentUser1 = new User();
        studentUser1.setEmail("student1@example.com");
        studentUser1.setPassword("password");
        studentUser1.setFirstName("Piotr");
        studentUser1.setLastName("Student");
        studentUser1.setRole(studentRole);
        studentUser1.setIsActive(true);
        userRepository.save(studentUser1);

        User studentUser2 = new User();
        studentUser2.setEmail("student2@example.com");
        studentUser2.setPassword("password");
        studentUser2.setFirstName("Katarzyna");
        studentUser2.setLastName("Uczennica");
        studentUser2.setRole(studentRole);
        studentUser2.setIsActive(true);
        userRepository.save(studentUser2);

        teacher1 = new Teacher();
        teacher1.setUser(teacherUser1);
        teacher1.setTitle("Prof.");
        teacherRepository.save(teacher1);

        teacher2 = new Teacher();
        teacher2.setUser(teacherUser2);
        teacher2.setTitle("Dr");
        teacherRepository.save(teacher2);

        student1 = new Student();
        student1.setUser(studentUser1);
        student1.setIndexNumber("123456");
        studentRepository.save(student1);

        student2 = new Student();
        student2.setUser(studentUser2);
        student2.setIndexNumber("654321");
        studentRepository.save(student2);

        course1 = new Course();
        course1.setCourseName("Programowanie");
        course1.setCourseCode("PRG101");
        course1.setDescription("Kurs programowania");
        course1.setCredits(5);
        courseRepository.save(course1);

        course2 = new Course();
        course2.setCourseName("Matematyka");
        course2.setCourseCode("MAT101");
        course2.setDescription("Kurs matematyki");
        course2.setCredits(6);
        courseRepository.save(course2);

        courseGroup1 = new CourseGroup();
        courseGroup1.setCourse(course1);
        courseGroup1.setTeacher(teacher1);
        courseGroup1.setGroupNumber(101);
        courseGroup1.setMaxCapacity(30);
        courseGroupRepository.save(courseGroup1);

        courseGroup2 = new CourseGroup();
        courseGroup2.setCourse(course1);
        courseGroup2.setTeacher(teacher2);
        courseGroup2.setGroupNumber(102);
        courseGroup2.setMaxCapacity(25);
        courseGroupRepository.save(courseGroup2);

        courseGroup3 = new CourseGroup();
        courseGroup3.setCourse(course2);
        courseGroup3.setTeacher(teacher1);
        courseGroup3.setGroupNumber(201);
        courseGroup3.setMaxCapacity(20);
        courseGroupRepository.save(courseGroup3);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setStudent(student1);
        enrollment1.setGroup(courseGroup1);
        enrollment1.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setStudent(student2);
        enrollment2.setGroup(courseGroup2);
        enrollment2.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment2);
    }

    @Test
    @DisplayName("Should check if course group exists by teacher and student")
    void shouldCheckIfCourseGroupExistsByTeacherAndStudent() {
        boolean exists = courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher1, student1);
        boolean notExists = courseGroupRepository.existsByTeacherAndEnrollmentsStudent(teacher1, student2);

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should count course groups by teacher")
    void shouldCountCourseGroupsByTeacher() {
        Long count1 = courseGroupRepository.countByTeacher(teacher1);
        Long count2 = courseGroupRepository.countByTeacher(teacher2);

        assertEquals(2, count1);
        assertEquals(1, count2);
    }

    @Test
    @DisplayName("Should find course groups by teacher with course")
    void shouldFindByTeacherWithCourse() {
        List<CourseGroup> groups = courseGroupRepository.findByTeacherWithCourse(teacher1);

        assertEquals(2, groups.size());
        assertTrue(groups.stream().allMatch(group -> group.getTeacher().equals(teacher1)));
        assertTrue(groups.stream().allMatch(group -> group.getCourse() != null));
    }

    @Test
    @DisplayName("Should find course groups by teacher and course with details")
    void shouldFindByTeacherAndCourseWithDetails() {
        List<CourseGroup> groups = courseGroupRepository.findByTeacherAndCourseWithDetails(teacher1, course1);

        assertEquals(1, groups.size());
        assertEquals(teacher1, groups.get(0).getTeacher());
        assertEquals(course1, groups.get(0).getCourse());
        assertNotNull(groups.get(0).getTeacher().getUser());
    }

    @Test
    @DisplayName("Should find course groups by teacher with details")
    void shouldFindByTeacherWithDetails() {
        List<CourseGroup> groups = courseGroupRepository.findByTeacherWithDetails(teacher1);

        assertEquals(2, groups.size());
        assertTrue(groups.stream().allMatch(group -> group.getTeacher().equals(teacher1)));
        assertTrue(groups.stream().allMatch(group -> group.getCourse() != null));
        assertTrue(groups.stream().allMatch(group -> group.getTeacher().getUser() != null));
    }

    @Test
    @DisplayName("Should find course groups by course with details")
    void shouldFindByCourseWithDetails() {
        List<CourseGroup> groups = courseGroupRepository.findByCourseWithDetails(course1);

        assertEquals(2, groups.size());
        assertTrue(groups.stream().allMatch(group -> group.getCourse().equals(course1)));
        assertTrue(groups.stream().allMatch(group -> group.getTeacher() != null));
        assertTrue(groups.stream().allMatch(group -> group.getTeacher().getUser() != null));
    }

    @Test
    @DisplayName("Should find course group by id with details")
    void shouldFindByIdWithDetails() {
        Optional<CourseGroup> group = courseGroupRepository.findByIdWithDetails(courseGroup1.getCourseGroupId());

        assertTrue(group.isPresent());
        assertEquals(courseGroup1.getCourseGroupId(), group.get().getCourseGroupId());
        assertNotNull(group.get().getCourse());
        assertNotNull(group.get().getTeacher());
        assertNotNull(group.get().getTeacher().getUser());
    }

    @Test
    @DisplayName("Should find course groups by course")
    void shouldFindByCourse() {
        List<CourseGroup> groups = courseGroupRepository.findByCourse(course1);

        assertEquals(2, groups.size());
        assertTrue(groups.stream().allMatch(group -> group.getCourse().equals(course1)));
    }

    @Test
    @DisplayName("Should check if course group exists by course and group number excluding specific group")
    void shouldCheckIfExistsByCourseAndGroupNumberAndCourseGroupIdNot() {
        boolean exists = courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course1, 102, courseGroup1.getCourseGroupId());
        boolean notExists = courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course1, 103, courseGroup1.getCourseGroupId());

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should check if course group exists by course and group number")
    void shouldCheckIfExistsByCourseAndGroupNumber() {
        boolean exists = courseGroupRepository.existsByCourseAndGroupNumber(course1, 101);
        boolean notExists = courseGroupRepository.existsByCourseAndGroupNumber(course1, 103);

        assertTrue(exists);
        assertFalse(notExists);
    }
}
