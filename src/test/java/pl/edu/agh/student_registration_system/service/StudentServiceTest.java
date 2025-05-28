package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.UpdateStudentDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private User user;
    private Student student;
    private Role studentRole;
    private Course course;
    private CourseGroup courseGroup;

    @BeforeEach
    void setUp() {
        studentRole = new Role(1L, RoleType.STUDENT, null);
        user = new User(1L, "John", "Doe", "password", "john.doe@example.com", true, studentRole, null, null);
        student = new Student(1L, "123456", user, null, null, null);
        user.setStudentProfile(student);

        course = new Course(1L, "Test Course", "TC101", "Test desc", 5, null, null);
        courseGroup = new CourseGroup(1L, 1, 10, course, null, null, null);
    }

    @Test
    void getStudentResponseById_Success() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        StudentResponse response = studentService.getStudentResponseById(1L);
        assertNotNull(response);
        assertEquals(student.getStudentId(), response.getStudentId());
        assertEquals(user.getEmail(), response.getUserInfo().getUsername());
    }

    @Test
    void getStudentResponseById_NotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentResponseById(1L));
    }

    @Test
    void getCurrentStudentResponse_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));
        StudentResponse response = studentService.getCurrentStudentResponse();
        assertNotNull(response);
        assertEquals(student.getStudentId(), response.getStudentId());
    }

    @Test
    void updateStudent_Success_EmailChanged() {
        UpdateStudentDTO dto = new UpdateStudentDTO();
        dto.setEmail("new.email@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setIsActive(true);

        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        when(userRepository.existsByEmail("new.email@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        StudentResponse response = studentService.updateStudent(1L, dto);
        assertNotNull(response);
        assertEquals("new.email@example.com", response.getUserInfo().getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void updateStudent_Success_NoActualChanges() {
        UpdateStudentDTO dto = new UpdateStudentDTO();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setIsActive(user.getIsActive());

        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.updateStudent(1L, dto);
        assertNotNull(response);
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void updateStudent_EmailAlreadyExists_ThrowsUserAlreadyExistsException() {
        UpdateStudentDTO dto = new UpdateStudentDTO();
        dto.setEmail("existing.email@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setIsActive(true);

        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        when(userRepository.existsByEmail("existing.email@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> studentService.updateStudent(1L, dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteStudentAndUser_Success() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        doNothing().when(userRepository).delete(user);
        studentService.deleteStudentAndUser(1L);
        verify(userRepository).delete(user);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchStudents_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> studentPage = new PageImpl<>(Collections.singletonList(student), pageable, 1);
        when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(studentPage);

        Page<StudentResponse> responsePage = studentService.searchStudents("John", pageable);
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(student.getStudentId(), responsePage.getContent().get(0).getStudentId());
    }

    @Test
    void getCurrentStudentGroups_Success() {
        Enrollment enrollment = new Enrollment(1L, LocalDateTime.now(), student, courseGroup);
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentWithGroupAndCourse(student)).thenReturn(Collections.singletonList(enrollment));
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(1);


        List<GroupResponse> groups = studentService.getCurrentStudentGroups();
        assertNotNull(groups);
        assertEquals(1, groups.size());
        assertEquals(courseGroup.getCourseGroupId(), groups.get(0).getGroupId());
    }

    @Test
    void getCurrentStudentGrades_WithCourseId_Success() {
        Grade grade = new Grade(1L, "5.0", LocalDateTime.now(), "Good", student, course, null);
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeRepository.findByStudentAndCourseWithDetails(student, course)).thenReturn(Collections.singletonList(grade));

        List<GradeResponse> grades = studentService.getCurrentStudentGrades(1L);
        assertNotNull(grades);
        assertEquals(1, grades.size());
        assertEquals("5.0", grades.get(0).getGradeValue());
    }

    @Test
    void getCurrentStudentGrades_WithoutCourseId_Success() {
        Grade grade1 = new Grade(1L, "5.0", LocalDateTime.now(), "Good", student, course, null);
        Course anotherCourse = new Course(2L, "Another Course", "AC202", "Desc", 4, null, null);
        Grade grade2 = new Grade(2L, "4.0", LocalDateTime.now(), "Okay", student, anotherCourse, null);

        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudentWithDetails(student)).thenReturn(List.of(grade1, grade2));

        List<GradeResponse> grades = studentService.getCurrentStudentGrades(null);
        assertNotNull(grades);
        assertEquals(2, grades.size());
    }


    @Test
    @SuppressWarnings("unchecked")
    void getCurrentStudentAttendance_Success() {
        Meeting meeting = new Meeting(1L, 1, LocalDateTime.now(), "Topic", courseGroup, null);
        Attendance attendance = new Attendance(1L, AttendanceStatus.PRESENT, meeting, student, null);
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));
        when(attendanceRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(attendance));

        List<AttendanceResponse> attendances = studentService.getCurrentStudentAttendance(1L, 1L, 1);
        assertNotNull(attendances);
        assertEquals(1, attendances.size());
        assertEquals(AttendanceStatus.PRESENT.name(), attendances.get(0).getStatus());
    }

    @Test
    void findStudentById_Success() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(student));
        Student found = studentService.findStudentById(1L);
        assertNotNull(found);
        assertEquals(student.getStudentId(), found.getStudentId());
    }

    @Test
    void findCurrentStudentEntity_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));
        Student current = studentService.findCurrentStudentEntity();
        assertNotNull(current);
        assertEquals(student.getStudentId(), current.getStudentId());
    }

    @Test
    void mapToStudentResponse_Success() {
        StudentResponse response = studentService.mapToStudentResponse(student);
        assertNotNull(response);
        assertEquals(student.getStudentId(), response.getStudentId());
        assertEquals(student.getIndexNumber(), response.getIndexNumber());
        assertNotNull(response.getUserInfo());
        assertEquals(user.getEmail(), response.getUserInfo().getUsername());
        assertEquals(RoleType.STUDENT.name(), response.getUserInfo().getRoles().get(0));
    }

    @Test
    void mapToStudentResponse_UserIsNull_ThrowsIllegalStateException() {
        student.setUser(null);
        assertThrows(IllegalStateException.class, () -> studentService.mapToStudentResponse(student));
    }

    @Test
    void mapToStudentResponse_UserRoleIsNull_ThrowsIllegalStateException() {
        user.setRole(null);
        student.setUser(user);
        assertThrows(IllegalStateException.class, () -> studentService.mapToStudentResponse(student));
    }

}