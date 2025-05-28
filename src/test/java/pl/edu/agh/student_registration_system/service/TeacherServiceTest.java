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
import pl.edu.agh.student_registration_system.payload.dto.UpdateTeacherDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.TeacherResponse;
import pl.edu.agh.student_registration_system.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private User user;
    private Teacher teacher;
    private Role teacherRole;
    private Course course;
    private CourseGroup courseGroup;

    @BeforeEach
    void setUp() {
        teacherRole = new Role(1L, RoleType.TEACHER, null);
        user = new User(1L, "Jane", "Smith", "securepass", "jane.smith@example.com", true, teacherRole, null, null);
        teacher = new Teacher(1L, "Dr.", user, null, null, null);
        user.setTeacherProfile(teacher);

        course = new Course(1L, "Advanced Java", "CS305", "Deep dive into Java", 6, null, null);
        courseGroup = new CourseGroup(1L, 1, 20, course, teacher, null, null);
    }

    @Test
    void findCurrentTeacherEntity_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(teacherRepository.findByUser(user)).thenReturn(Optional.of(teacher));
        Teacher foundTeacher = teacherService.findCurrentTeacherEntity();
        assertNotNull(foundTeacher);
        assertEquals(teacher.getTeacherId(), foundTeacher.getTeacherId());
    }

    @Test
    void findCurrentTeacherEntity_ProfileNotFound_ThrowsResourceNotFoundException() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(teacherRepository.findByUser(user)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> teacherService.findCurrentTeacherEntity());
    }

    @Test
    void getTeacherResponseById_Success() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        TeacherResponse response = teacherService.getTeacherResponseById(1L);
        assertNotNull(response);
        assertEquals(teacher.getTeacherId(), response.getTeacherId());
        assertEquals(user.getEmail(), response.getUserInfo().getUsername());
    }

    @Test
    void getCurrentTeacherResponse_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(teacherRepository.findByUser(user)).thenReturn(Optional.of(teacher));
        TeacherResponse response = teacherService.getCurrentTeacherResponse();
        assertNotNull(response);
        assertEquals(teacher.getTeacherId(), response.getTeacherId());
    }

    @Test
    void updateTeacher_Success_AllFieldsChanged() {
        UpdateTeacherDTO dto = new UpdateTeacherDTO();
        dto.setEmail("new.jane@example.com");
        dto.setFirstName("Janet");
        dto.setLastName("Smithson");
        dto.setIsActive(false);
        dto.setTitle("Prof.");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(userRepository.existsByEmail("new.jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        TeacherResponse response = teacherService.updateTeacher(1L, dto);
        assertNotNull(response);
        assertEquals("new.jane@example.com", response.getUserInfo().getUsername());
        assertEquals("Janet", response.getUserInfo().getFirstName());
        assertEquals("Prof.", response.getTitle());
        assertFalse(response.getUserInfo().getIsActive());
        verify(userRepository).save(user);
        verify(teacherRepository).save(teacher);
    }

    @Test
    void updateTeacher_EmailAlreadyExists_ThrowsUserAlreadyExistsException() {
        UpdateTeacherDTO dto = new UpdateTeacherDTO();
        dto.setEmail("existing.email@example.com");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(userRepository.existsByEmail("existing.email@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> teacherService.updateTeacher(1L, dto));
    }

    @Test
    void deleteTeacherAndUser_Success_NoAssignedGroups() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(courseGroupRepository.countByTeacher(teacher)).thenReturn(0L);
        doNothing().when(userRepository).delete(user);

        teacherService.deleteTeacherAndUser(1L);
        verify(userRepository).delete(user);
        verify(teacherRepository, never()).delete(teacher);
    }

    @Test
    void deleteTeacherAndUser_Success_TeacherHasNoUser() {
        teacher.setUser(null);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(courseGroupRepository.countByTeacher(teacher)).thenReturn(0L);
        doNothing().when(teacherRepository).delete(teacher);

        teacherService.deleteTeacherAndUser(1L);
        verify(userRepository, never()).delete(any(User.class));
        verify(teacherRepository).delete(teacher);
    }


    @Test
    void deleteTeacherAndUser_HasAssignedGroups_ThrowsIllegalStateException() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(courseGroupRepository.countByTeacher(teacher)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> teacherService.deleteTeacherAndUser(1L));
        verify(userRepository, never()).delete(any(User.class));
        verify(teacherRepository, never()).delete(any(Teacher.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchTeachers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Teacher> teacherPage = new PageImpl<>(Collections.singletonList(teacher), pageable, 1);
        when(teacherRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(teacherPage);

        Page<TeacherResponse> responsePage = teacherService.searchTeachers("Jane", pageable);
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(teacher.getTeacherId(), responsePage.getContent().get(0).getTeacherId());
    }

    @Test
    void getCurrentTeacherCourses_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(teacherRepository.findByUser(user)).thenReturn(Optional.of(teacher));
        when(courseGroupRepository.findByTeacherWithCourse(teacher)).thenReturn(Collections.singletonList(courseGroup));

        List<CourseResponse> courses = teacherService.getCurrentTeacherCourses();
        assertNotNull(courses);
        assertEquals(1, courses.size());
        assertEquals(course.getCourseId(), courses.get(0).getCourseId());
    }

    @Test
    void getCurrentTeacherGroups_WithCourseId_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(teacherRepository.findByUser(user)).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseGroupRepository.findByTeacherAndCourseWithDetails(teacher, course)).thenReturn(Collections.singletonList(courseGroup));
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(5);

        List<GroupResponse> groups = teacherService.getCurrentTeacherGroups(1L);
        assertNotNull(groups);
        assertEquals(1, groups.size());
        assertEquals(courseGroup.getCourseGroupId(), groups.get(0).getGroupId());
    }

    @Test
    void getCurrentTeacherGroups_WithoutCourseId_Success() {
        when(userService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(teacherRepository.findByUser(user)).thenReturn(Optional.of(teacher));
        when(courseGroupRepository.findByTeacherWithDetails(teacher)).thenReturn(Collections.singletonList(courseGroup));
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(5);

        List<GroupResponse> groups = teacherService.getCurrentTeacherGroups(null);
        assertNotNull(groups);
        assertEquals(1, groups.size());
        assertEquals(courseGroup.getCourseGroupId(), groups.get(0).getGroupId());
    }


    @Test
    void findById_Success() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        Teacher found = teacherService.findById(1L);
        assertNotNull(found);
        assertEquals(teacher.getTeacherId(), found.getTeacherId());
    }

    @Test
    void findById_NotFound_ThrowsResourceNotFoundException() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> teacherService.findById(1L));
    }

}