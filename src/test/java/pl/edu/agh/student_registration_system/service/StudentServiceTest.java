package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.mockito.ArgumentMatchers.*;
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

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private Student testStudent;
    private User testUser;
    private Role testRole;
    private Course testCourse;
    private CourseGroup testGroup;
    private Enrollment testEnrollment;
    private Grade testGrade;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName(RoleType.STUDENT);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setIsActive(true);
        testUser.setRole(testRole);

        testStudent = new Student();
        testStudent.setStudentId(1L);
        testStudent.setIndexNumber("123456");
        testStudent.setUser(testUser);

        testCourse = new Course();
        testCourse.setCourseId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");

        testGroup = new CourseGroup();
        testGroup.setCourseGroupId(1L);
        testGroup.setGroupNumber(1);
        testGroup.setMaxCapacity(30);
        testGroup.setCourse(testCourse);

        testEnrollment = new Enrollment();
        testEnrollment.setEnrollmentId(1L);
        testEnrollment.setStudent(testStudent);
        testEnrollment.setGroup(testGroup);
        testEnrollment.setEnrollmentDate(LocalDateTime.now());

        testGrade = new Grade();
        testGrade.setGradeId(1L);
        testGrade.setGradeValue("5.0");
        testGrade.setGradeDate(LocalDateTime.now());
        testGrade.setComment("Excellent work");
        testGrade.setStudent(testStudent);
        testGrade.setCourse(testCourse);
    }

    @Test
    void getStudentResponseById_ShouldReturnStudentResponse_WhenStudentExists() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(testStudent));

        StudentResponse result = studentService.getStudentResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals("123456", result.getIndexNumber());
        assertNotNull(result.getUserInfo());
        assertEquals("John", result.getUserInfo().getFirstName());
        assertEquals("Doe", result.getUserInfo().getLastName());

        verify(studentRepository).findByIdWithUser(1L);
    }

    @Test
    void getStudentResponseById_ShouldThrowResourceNotFoundException_WhenStudentDoesNotExist() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentResponseById(1L));

        verify(studentRepository).findByIdWithUser(1L);
    }

    @Test
    void getCurrentStudentResponse_ShouldReturnStudentResponse_WhenCurrentStudentExists() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));

        StudentResponse result = studentService.getCurrentStudentResponse();

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals("123456", result.getIndexNumber());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
    }

    @Test
    void updateStudent_ShouldReturnUpdatedStudentResponse_WhenUpdateIsValid() {
        UpdateStudentDTO updateStudentDTO = new UpdateStudentDTO();
        updateStudentDTO.setFirstName("Jane");
        updateStudentDTO.setLastName("Smith");
        updateStudentDTO.setEmail("jane.smith@example.com");
        updateStudentDTO.setIsActive(true);

        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(testStudent));
        when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        StudentResponse result = studentService.updateStudent(1L, updateStudentDTO);

        assertNotNull(result);
        verify(studentRepository).findByIdWithUser(1L);
        verify(userRepository).existsByEmail("jane.smith@example.com");
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("Jane", capturedUser.getFirstName());
        assertEquals("Smith", capturedUser.getLastName());
        assertEquals("jane.smith@example.com", capturedUser.getEmail());
        assertTrue(capturedUser.getIsActive());
    }

    @Test
    void updateStudent_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        UpdateStudentDTO updateStudentDTO = new UpdateStudentDTO();
        updateStudentDTO.setEmail("existing@example.com");

        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(testStudent));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> studentService.updateStudent(1L, updateStudentDTO));

        verify(studentRepository).findByIdWithUser(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteStudentAndUser_ShouldDeleteUserAndStudent_WhenStudentExists() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(testStudent));
        doNothing().when(userRepository).delete(testUser);

        studentService.deleteStudentAndUser(1L);

        verify(studentRepository).findByIdWithUser(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void searchStudents_ShouldReturnPageOfStudentResponses() {
        Pageable pageable = Pageable.unpaged();
        Page<Student> studentPage = new PageImpl<>(List.of(testStudent));

        when(studentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(studentPage);

        Page<StudentResponse> result = studentService.searchStudents("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getStudentId());

        verify(studentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getCurrentStudentGroups_ShouldReturnListOfGroupResponses_WhenStudentIsEnrolled() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentWithGroupAndCourse(testStudent)).thenReturn(List.of(testEnrollment));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        List<GroupResponse> result = studentService.getCurrentStudentGroups();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGroupId());
        assertEquals(1, result.get(0).getGroupNumber());
        assertEquals(15, result.get(0).getEnrolledCount());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
        verify(enrollmentRepository).findByStudentWithGroupAndCourse(testStudent);
        verify(enrollmentRepository).countByGroup(testGroup);
    }

    @Test
    void getCurrentStudentGroups_ShouldReturnEmptyList_WhenStudentIsNotEnrolled() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));
        when(enrollmentRepository.findByStudentWithGroupAndCourse(testStudent)).thenReturn(Collections.emptyList());

        List<GroupResponse> result = studentService.getCurrentStudentGroups();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
        verify(enrollmentRepository).findByStudentWithGroupAndCourse(testStudent);
    }

    @Test
    void getCurrentStudentGrades_ShouldReturnListOfGradeResponses_WhenCourseIdIsProvided() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(gradeRepository.findByStudentAndCourseWithDetails(testStudent, testCourse)).thenReturn(List.of(testGrade));

        List<GradeResponse> result = studentService.getCurrentStudentGrades(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGradeId());
        assertEquals("5.0", result.get(0).getGradeValue());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
        verify(courseRepository).findById(1L);
        verify(gradeRepository).findByStudentAndCourseWithDetails(testStudent, testCourse);
    }

    @Test
    void getCurrentStudentGrades_ShouldReturnListOfAllGradeResponses_WhenCourseIdIsNull() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));
        when(gradeRepository.findByStudentWithDetails(testStudent)).thenReturn(List.of(testGrade));

        List<GradeResponse> result = studentService.getCurrentStudentGrades(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGradeId());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
        verify(courseRepository, never()).findById(anyLong());
        verify(gradeRepository).findByStudentWithDetails(testStudent);
    }

    @Test
    void getCurrentStudentGrades_ShouldThrowResourceNotFoundException_WhenCourseDoesNotExist() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getCurrentStudentGrades(1L));

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
        verify(courseRepository).findById(1L);
        verify(gradeRepository, never()).findByStudentAndCourseWithDetails(any(), any());
    }

    @Test
    void getCurrentStudentAttendance_ShouldReturnNull() {
        List<AttendanceResponse> result = studentService.getCurrentStudentAttendance(1L, 1L, 1);

        assertNull(result);
    }

    @Test
    void findStudentById_ShouldReturnStudent_WhenStudentExists() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.of(testStudent));

        Student result = studentService.findStudentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals("123456", result.getIndexNumber());

        verify(studentRepository).findByIdWithUser(1L);
    }

    @Test
    void findStudentById_ShouldThrowResourceNotFoundException_WhenStudentDoesNotExist() {
        when(studentRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findStudentById(1L));

        verify(studentRepository).findByIdWithUser(1L);
    }

    @Test
    void findCurrentStudentEntity_ShouldReturnStudent_WhenCurrentUserHasStudentProfile() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.of(testStudent));

        Student result = studentService.findCurrentStudentEntity();

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
    }

    @Test
    void findCurrentStudentEntity_ShouldThrowResourceNotFoundException_WhenCurrentUserHasNoStudentProfile() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(studentRepository.findByUser(testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findCurrentStudentEntity());

        verify(userService).getCurrentAuthenticatedUser();
        verify(studentRepository).findByUser(testUser);
    }

    @Test
    void mapToStudentResponse_ShouldReturnStudentResponse_WhenStudentIsValid() {
        StudentResponse result = studentService.mapToStudentResponse(testStudent);

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals("123456", result.getIndexNumber());
        assertNotNull(result.getUserInfo());
        assertEquals(1L, result.getUserInfo().getId());
        assertEquals("john.doe@example.com", result.getUserInfo().getUsername());
        assertEquals("John", result.getUserInfo().getFirstName());
        assertEquals("Doe", result.getUserInfo().getLastName());
        assertTrue(result.getUserInfo().getIsActive());
        assertEquals(1, result.getUserInfo().getRoles().size());
        assertEquals("STUDENT", result.getUserInfo().getRoles().get(0));
    }

    @Test
    void mapToStudentResponse_ShouldThrowIllegalStateException_WhenStudentHasNoUser() {
        testStudent.setUser(null);

        assertThrows(IllegalStateException.class, () -> studentService.mapToStudentResponse(testStudent));
    }
}
