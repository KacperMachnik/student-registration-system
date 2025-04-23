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
class GradeRepositoryTest {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private CourseRepository courseRepository;

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
    private Student student1;
    private Student student2;
    private Teacher teacher1;
    private Grade grade1;
    private Grade grade2;
    private Grade grade3;
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

        grade1 = new Grade();
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setTeacher(teacher1);
        grade1.setGradeValue("5.0");
        grade1.setGradeDate(LocalDateTime.now().minusDays(10));
        grade1.setComment("Excellent work");
        gradeRepository.save(grade1);

        grade2 = new Grade();
        grade2.setStudent(student1);
        grade2.setCourse(course2);
        grade2.setTeacher(teacher1);
        grade2.setGradeValue("4.5");
        grade2.setGradeDate(LocalDateTime.now().minusDays(5));
        grade2.setComment("Very good");
        gradeRepository.save(grade2);

        grade3 = new Grade();
        grade3.setStudent(student2);
        grade3.setCourse(course1);
        grade3.setTeacher(teacher1);
        grade3.setGradeValue("4.0");
        grade3.setGradeDate(LocalDateTime.now().minusDays(3));
        grade3.setComment("Good job");
        gradeRepository.save(grade3);
    }

    @Test
    @DisplayName("Should find grades by student and course with details")
    void shouldFindByStudentAndCourseWithDetails() {
        List<Grade> grades = gradeRepository.findByStudentAndCourseWithDetails(student1, course1);

        assertEquals(1, grades.size());
        assertEquals(grade1, grades.get(0));
        assertNotNull(grades.get(0).getStudent().getUser());
        assertNotNull(grades.get(0).getCourse());
        assertNotNull(grades.get(0).getTeacher().getUser());
    }

    @Test
    @DisplayName("Should find grades by student with details")
    void shouldFindByStudentWithDetails() {
        List<Grade> grades = gradeRepository.findByStudentWithDetails(student1);

        assertEquals(2, grades.size());
        assertTrue(grades.contains(grade1));
        assertTrue(grades.contains(grade2));
        assertFalse(grades.contains(grade3));
        assertTrue(grades.stream().allMatch(g -> g.getStudent().getUser() != null));
        assertTrue(grades.stream().allMatch(g -> g.getCourse() != null));
        assertTrue(grades.stream().allMatch(g -> g.getTeacher().getUser() != null));
    }

    @Test
    @DisplayName("Should save new grade")
    void shouldSaveNewGrade() {
        Grade newGrade = new Grade();
        newGrade.setStudent(student2);
        newGrade.setCourse(course2);
        newGrade.setTeacher(teacher1);
        newGrade.setGradeValue("3.5");
        newGrade.setGradeDate(LocalDateTime.now());
        newGrade.setComment("Needs improvement");

        Grade savedGrade = gradeRepository.save(newGrade);

        assertNotNull(savedGrade.getGradeId());

        Optional<Grade> foundGrade = gradeRepository.findById(savedGrade.getGradeId());
        assertTrue(foundGrade.isPresent());
        assertEquals("3.5", foundGrade.get().getGradeValue());
        assertEquals("Needs improvement", foundGrade.get().getComment());
    }

    @Test
    @DisplayName("Should update existing grade")
    void shouldUpdateExistingGrade() {
        grade1.setGradeValue("5.5");
        grade1.setComment("Outstanding performance");

        Grade updatedGrade = gradeRepository.save(grade1);

        assertEquals(grade1.getGradeId(), updatedGrade.getGradeId());
        assertEquals("5.5", updatedGrade.getGradeValue());
        assertEquals("Outstanding performance", updatedGrade.getComment());

        Optional<Grade> foundGrade = gradeRepository.findById(grade1.getGradeId());
        assertTrue(foundGrade.isPresent());
        assertEquals("5.5", foundGrade.get().getGradeValue());
        assertEquals("Outstanding performance", foundGrade.get().getComment());
    }

    @Test
    @DisplayName("Should delete grade")
    void shouldDeleteGrade() {
        gradeRepository.delete(grade3);

        Optional<Grade> foundGrade = gradeRepository.findById(grade3.getGradeId());
        assertFalse(foundGrade.isPresent());
    }
}
