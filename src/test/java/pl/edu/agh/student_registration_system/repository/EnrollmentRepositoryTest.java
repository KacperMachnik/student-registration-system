package pl.edu.agh.student_registration_system.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.edu.agh.student_registration_system.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseGroupRepository courseGroupRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Course course1;
    private Course course2;
    private CourseGroup group1;
    private CourseGroup group2;
    private CourseGroup group3;
    private Student student1;
    private Student student2;
    private Teacher teacher1;
    private Enrollment enrollment1;
    private Enrollment enrollment2;
    private Enrollment enrollment3;
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

        User teacherUser = new User();
        teacherUser.setEmail("teacher@example.com");
        teacherUser.setPassword("password");
        teacherUser.setFirstName("Jan");
        teacherUser.setLastName("Profesor");
        teacherUser.setRole(teacherRole);
        teacherUser.setIsActive(true);
        userRepository.save(teacherUser);

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
        teacher1.setUser(teacherUser);
        teacher1.setTitle("Prof.");
        teacherRepository.save(teacher1);

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

        group1 = new CourseGroup();
        group1.setCourse(course1);
        group1.setTeacher(teacher1);
        group1.setGroupNumber(101);
        group1.setMaxCapacity(30);
        courseGroupRepository.save(group1);

        group2 = new CourseGroup();
        group2.setCourse(course1);
        group2.setTeacher(teacher1);
        group2.setGroupNumber(102);
        group2.setMaxCapacity(25);
        courseGroupRepository.save(group2);

        group3 = new CourseGroup();
        group3.setCourse(course2);
        group3.setTeacher(teacher1);
        group3.setGroupNumber(201);
        group3.setMaxCapacity(20);
        courseGroupRepository.save(group3);

        enrollment1 = new Enrollment();
        enrollment1.setStudent(student1);
        enrollment1.setGroup(group1);
        enrollment1.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment1);

        enrollment2 = new Enrollment();
        enrollment2.setStudent(student1);
        enrollment2.setGroup(group3);
        enrollment2.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment2);

        enrollment3 = new Enrollment();
        enrollment3.setStudent(student2);
        enrollment3.setGroup(group2);
        enrollment3.setEnrollmentDate(LocalDateTime.now());
        enrollmentRepository.save(enrollment3);
    }

    @Test
    @DisplayName("Should find enrollments by student")
    void shouldFindByStudent() {
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student1);

        assertEquals(2, enrollments.size());
        assertTrue(enrollments.contains(enrollment1));
        assertTrue(enrollments.contains(enrollment2));
        assertFalse(enrollments.contains(enrollment3));
    }

    @Test
    @DisplayName("Should count enrollments by group")
    void shouldCountByGroup() {
        Integer count1 = enrollmentRepository.countByGroup(group1);
        Integer count2 = enrollmentRepository.countByGroup(group2);
        Integer count3 = enrollmentRepository.countByGroup(group3);

        assertEquals(1, count1);
        assertEquals(1, count2);
        assertEquals(1, count3);
    }

    @Test
    @DisplayName("Should find enrollments by student with group and course")
    void shouldFindByStudentWithGroupAndCourse() {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentWithGroupAndCourse(student1);

        assertEquals(2, enrollments.size());
        assertTrue(enrollments.stream().anyMatch(e -> e.getGroup().equals(group1)));
        assertTrue(enrollments.stream().anyMatch(e -> e.getGroup().equals(group3)));
        assertTrue(enrollments.stream().allMatch(e -> e.getGroup().getCourse() != null));
        assertTrue(enrollments.stream().allMatch(e -> e.getGroup().getTeacher() != null));
        assertTrue(enrollments.stream().allMatch(e -> e.getGroup().getTeacher().getUser() != null));
    }

    @Test
    @DisplayName("Should find students by group id")
    void shouldFindStudentsByGroupId() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Student> studentsPage1 = enrollmentRepository.findStudentsByGroupId(group1.getCourseGroupId(), pageable);
        Page<Student> studentsPage2 = enrollmentRepository.findStudentsByGroupId(group2.getCourseGroupId(), pageable);

        assertEquals(1, studentsPage1.getTotalElements());
        assertEquals(student1, studentsPage1.getContent().get(0));

        assertEquals(1, studentsPage2.getTotalElements());
        assertEquals(student2, studentsPage2.getContent().get(0));
    }

    @Test
    @DisplayName("Should find course ids by student")
    void shouldFindCourseIdsByStudent() {
        List<Long> courseIds = enrollmentRepository.findCourseIdsByStudent(student1);

        assertEquals(2, courseIds.size());
        assertTrue(courseIds.contains(course1.getCourseId()));
        assertTrue(courseIds.contains(course2.getCourseId()));
    }

    @Test
    @DisplayName("Should check if enrollment exists by student and course")
    void shouldCheckIfExistsByStudentAndCourse() {
        boolean exists1 = enrollmentRepository.existsByStudentAndGroup_Course(student1, course1);
        boolean exists2 = enrollmentRepository.existsByStudentAndGroup_Course(student2, course2);

        assertTrue(exists1);
        assertFalse(exists2);
    }

    @Test
    @DisplayName("Should find enrollment by student and group id")
    void shouldFindByStudentAndGroupId() {
        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentAndGroup_CourseGroupId(student1, group1.getCourseGroupId());

        assertTrue(enrollment.isPresent());
        assertEquals(enrollment1, enrollment.get());
    }

    @Test
    @DisplayName("Should find enrollments by group")
    void shouldFindByGroup() {
        List<Enrollment> enrollments = enrollmentRepository.findByGroup(group1);

        assertEquals(1, enrollments.size());
        assertEquals(enrollment1, enrollments.get(0));
    }

    @Test
    @DisplayName("Should check if enrollment exists by student and group id")
    void shouldCheckIfExistsByStudentAndGroupId() {
        boolean exists1 = enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student1, group1.getCourseGroupId());
        boolean exists2 = enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student2, group3.getCourseGroupId());

        assertTrue(exists1);
        assertFalse(exists2);
    }

    @Test
    @DisplayName("Should save new enrollment")
    void shouldSaveNewEnrollment() {
        Enrollment newEnrollment = new Enrollment();
        newEnrollment.setStudent(student2);
        newEnrollment.setGroup(group3);
        newEnrollment.setEnrollmentDate(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(newEnrollment);

        assertNotNull(savedEnrollment.getEnrollmentId());

        Optional<Enrollment> foundEnrollment = enrollmentRepository.findById(savedEnrollment.getEnrollmentId());
        assertTrue(foundEnrollment.isPresent());
        assertEquals(student2, foundEnrollment.get().getStudent());
        assertEquals(group3, foundEnrollment.get().getGroup());
    }

    @Test
    @DisplayName("Should delete enrollment")
    void shouldDeleteEnrollment() {
        enrollmentRepository.delete(enrollment1);

        Optional<Enrollment> foundEnrollment = enrollmentRepository.findById(enrollment1.getEnrollmentId());
        assertFalse(foundEnrollment.isPresent());
    }
}
