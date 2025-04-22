package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.Predicate;
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
import pl.edu.agh.student_registration_system.payload.dto.UpdateTeacherDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<Teacher> teacherCaptor;

    private Teacher testTeacher;
    private User testUser;
    private Role testRole;
    private Course testCourse;
    private CourseGroup testGroup;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName(RoleType.TEACHER);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setIsActive(true);
        testUser.setRole(testRole);

        testTeacher = new Teacher();
        testTeacher.setTeacherId(1L);
        testTeacher.setTitle("Professor");
        testTeacher.setUser(testUser);

        testCourse = new Course();
        testCourse.setCourseId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");

        testGroup = new CourseGroup();
        testGroup.setCourseGroupId(1L);
        testGroup.setGroupNumber(1);
        testGroup.setMaxCapacity(30);
        testGroup.setCourse(testCourse);
        testGroup.setTeacher(testTeacher);
    }

    @Test
    void findCurrentTeacherEntity_ShouldReturnTeacher_WhenCurrentUserHasTeacherProfile() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.of(testTeacher));

        Teacher result = teacherService.findCurrentTeacherEntity();

        assertNotNull(result);
        assertEquals(1L, result.getTeacherId());
        assertEquals("Professor", result.getTitle());

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
    }

    @Test
    void findCurrentTeacherEntity_ShouldThrowResourceNotFoundException_WhenCurrentUserHasNoTeacherProfile() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.findCurrentTeacherEntity());

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
    }

    @Test
    void getTeacherResponseById_ShouldReturnTeacherResponse_WhenTeacherExists() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));

        TeacherResponse result = teacherService.getTeacherResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTeacherId());
        assertEquals("Professor", result.getTitle());
        assertNotNull(result.getUserInfo());
        assertEquals("John", result.getUserInfo().getFirstName());
        assertEquals("Doe", result.getUserInfo().getLastName());

        verify(teacherRepository).findById(1L);
    }

    @Test
    void getTeacherResponseById_ShouldThrowResourceNotFoundException_WhenTeacherDoesNotExist() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getTeacherResponseById(1L));

        verify(teacherRepository).findById(1L);
    }

    @Test
    void getCurrentTeacherResponse_ShouldReturnTeacherResponse_WhenCurrentTeacherExists() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.of(testTeacher));

        TeacherResponse result = teacherService.getCurrentTeacherResponse();

        assertNotNull(result);
        assertEquals(1L, result.getTeacherId());
        assertEquals("Professor", result.getTitle());

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
    }

    @Test
    void updateTeacher_ShouldReturnUpdatedTeacherResponse_WhenUpdateIsValid() {
        UpdateTeacherDTO updateTeacherDTO = new UpdateTeacherDTO();
        updateTeacherDTO.setFirstName("Jane");
        updateTeacherDTO.setLastName("Smith");
        updateTeacherDTO.setEmail("jane.smith@example.com");
        updateTeacherDTO.setIsActive(true);
        updateTeacherDTO.setTitle("Associate Professor");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);

        TeacherResponse result = teacherService.updateTeacher(1L, updateTeacherDTO);

        assertNotNull(result);
        verify(teacherRepository).findById(1L);
        verify(userRepository).existsByEmail("jane.smith@example.com");
        verify(userRepository).save(userCaptor.capture());
        verify(teacherRepository).save(teacherCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("Jane", capturedUser.getFirstName());
        assertEquals("Smith", capturedUser.getLastName());
        assertEquals("jane.smith@example.com", capturedUser.getEmail());
        assertTrue(capturedUser.getIsActive());

        Teacher capturedTeacher = teacherCaptor.getValue();
        assertEquals("Associate Professor", capturedTeacher.getTitle());
    }

    @Test
    void updateTeacher_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        UpdateTeacherDTO updateTeacherDTO = new UpdateTeacherDTO();
        updateTeacherDTO.setEmail("existing@example.com");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> teacherService.updateTeacher(1L, updateTeacherDTO));

        verify(teacherRepository).findById(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void deleteTeacherAndUser_ShouldDeleteUserAndTeacher_WhenTeacherHasNoGroups() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(courseGroupRepository.countByTeacher(testTeacher)).thenReturn(0L);
        doNothing().when(userRepository).delete(testUser);

        teacherService.deleteTeacherAndUser(1L);

        verify(teacherRepository).findById(1L);
        verify(courseGroupRepository).countByTeacher(testTeacher);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteTeacherAndUser_ShouldThrowIllegalStateException_WhenTeacherHasGroups() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(courseGroupRepository.countByTeacher(testTeacher)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> teacherService.deleteTeacherAndUser(1L));

        verify(teacherRepository).findById(1L);
        verify(courseGroupRepository).countByTeacher(testTeacher);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void searchTeachers_ShouldReturnPageOfTeacherResponses() {
        Pageable pageable = Pageable.unpaged();
        Page<Teacher> teacherPage = new PageImpl<>(List.of(testTeacher));

        when(teacherRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(teacherPage);

        Page<TeacherResponse> result = teacherService.searchTeachers("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getTeacherId());
        assertEquals("Professor", result.getContent().get(0).getTitle());

        verify(teacherRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getCurrentTeacherCourses_ShouldReturnListOfCourseResponses() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.of(testTeacher));
        when(courseGroupRepository.findByTeacherWithCourse(testTeacher)).thenReturn(List.of(testGroup));

        List<CourseResponse> result = teacherService.getCurrentTeacherCourses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCourseId());
        assertEquals("CS101", result.get(0).getCourseCode());

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
        verify(courseGroupRepository).findByTeacherWithCourse(testTeacher);
    }

    @Test
    void getCurrentTeacherGroups_ShouldReturnListOfGroupResponses_WhenCourseIdIsProvided() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.of(testTeacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.findByTeacherAndCourseWithDetails(testTeacher, testCourse)).thenReturn(List.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        List<GroupResponse> result = teacherService.getCurrentTeacherGroups(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGroupId());
        assertEquals(1, result.get(0).getGroupNumber());
        assertEquals(15, result.get(0).getEnrolledCount());

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).findByTeacherAndCourseWithDetails(testTeacher, testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
    }

    @Test
    void getCurrentTeacherGroups_ShouldReturnListOfAllGroupResponses_WhenCourseIdIsNull() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.of(testTeacher));
        when(courseGroupRepository.findByTeacherWithDetails(testTeacher)).thenReturn(List.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        List<GroupResponse> result = teacherService.getCurrentTeacherGroups(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGroupId());

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
        verify(courseRepository, never()).findById(anyLong());
        verify(courseGroupRepository).findByTeacherWithDetails(testTeacher);
        verify(enrollmentRepository).countByGroup(testGroup);
    }

    @Test
    void getCurrentTeacherGroups_ShouldThrowResourceNotFoundException_WhenCourseDoesNotExist() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        when(teacherRepository.findByUser(testUser)).thenReturn(Optional.of(testTeacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getCurrentTeacherGroups(1L));

        verify(userService).getCurrentAuthenticatedUser();
        verify(teacherRepository).findByUser(testUser);
        verify(courseRepository).findById(1L);
        verify(courseGroupRepository, never()).findByTeacherAndCourseWithDetails(any(), any());
    }

    @Test
    void findById_ShouldReturnTeacher_WhenTeacherExists() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));

        Teacher result = teacherService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTeacherId());
        assertEquals("Professor", result.getTitle());

        verify(teacherRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowResourceNotFoundException_WhenTeacherDoesNotExist() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.findById(1L));

        verify(teacherRepository).findById(1L);
    }
}
