package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.edu.agh.student_registration_system.exceptions.DeletionBlockedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGroupDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.CourseRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.repository.TeacherRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Captor
    private ArgumentCaptor<CourseGroup> courseGroupCaptor;

    private Course testCourse;
    private Teacher testTeacher;
    private CourseGroup testGroup;
    private Student testStudent;
    private User testUser;
    private CreateGroupDTO createGroupDTO;
    private UpdateGroupDTO updateGroupDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testTeacher = new Teacher();
        testTeacher.setTeacherId(1L);
        testTeacher.setTitle("Professor");
        testTeacher.setUser(testUser);

        testCourse = new Course();
        testCourse.setCourseId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");
        testCourse.setDescription("Basic concepts of computer science");
        testCourse.setCredits(5);

        testGroup = new CourseGroup();
        testGroup.setCourseGroupId(1L);
        testGroup.setGroupNumber(1);
        testGroup.setMaxCapacity(30);
        testGroup.setCourse(testCourse);
        testGroup.setTeacher(testTeacher);

        testStudent = new Student();
        testStudent.setStudentId(1L);
        testStudent.setIndexNumber("123456");
        testStudent.setUser(testUser);

        createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setCourseId(1L);
        createGroupDTO.setTeacherId(1L);
        createGroupDTO.setGroupNumber(2);
        createGroupDTO.setMaxCapacity(25);

        updateGroupDTO = new UpdateGroupDTO();
        updateGroupDTO.setTeacherId(2L);
        updateGroupDTO.setGroupNumber(3);
        updateGroupDTO.setMaxCapacity(35);
    }

    @Test
    void createGroup_ShouldReturnGroupResponse_WhenGroupCreatedSuccessfully() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.existsByCourseAndGroupNumber(testCourse, 2)).thenReturn(false);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(courseGroupRepository.save(any(CourseGroup.class))).thenAnswer(invocation -> {
            CourseGroup savedGroup = invocation.getArgument(0);
            savedGroup.setCourseGroupId(2L);
            return savedGroup;
        });
        when(enrollmentRepository.countByGroup(any(CourseGroup.class))).thenReturn(0);

        GroupResponse result = groupService.createGroup(createGroupDTO);

        assertNotNull(result);
        assertEquals(2L, result.getGroupId());
        assertEquals(2, result.getGroupNumber());
        assertEquals(25, result.getMaxCapacity());
        assertEquals(0, result.getEnrolledCount());
        assertNotNull(result.getCourse());
        assertEquals(1L, result.getCourse().getCourseId());
        assertNotNull(result.getTeacher());
        assertEquals(1L, result.getTeacher().getTeacherId());

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).existsByCourseAndGroupNumber(testCourse, 2);
        verify(teacherRepository).findById(1L);
        verify(courseGroupRepository).save(courseGroupCaptor.capture());

        CourseGroup capturedGroup = courseGroupCaptor.getValue();
        assertEquals(testCourse, capturedGroup.getCourse());
        assertEquals(testTeacher, capturedGroup.getTeacher());
        assertEquals(2, capturedGroup.getGroupNumber());
        assertEquals(25, capturedGroup.getMaxCapacity());
    }

    @Test
    void createGroup_ShouldThrowResourceNotFoundException_WhenCourseNotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.createGroup(createGroupDTO));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository, never()).existsByCourseAndGroupNumber(any(), anyInt());
        verify(teacherRepository, never()).findById(anyLong());
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void createGroup_ShouldThrowDataIntegrityViolationException_WhenGroupNumberExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.existsByCourseAndGroupNumber(testCourse, 2)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> groupService.createGroup(createGroupDTO));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).existsByCourseAndGroupNumber(testCourse, 2);
        verify(teacherRepository, never()).findById(anyLong());
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void getGroupResponseById_ShouldReturnGroupResponse_WhenGroupExists() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        GroupResponse result = groupService.getGroupResponseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getGroupId());
        assertEquals(1, result.getGroupNumber());
        assertEquals(30, result.getMaxCapacity());
        assertEquals(15, result.getEnrolledCount());
        assertNotNull(result.getCourse());
        assertEquals(1L, result.getCourse().getCourseId());
        assertNotNull(result.getTeacher());
        assertEquals(1L, result.getTeacher().getTeacherId());

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).countByGroup(testGroup);
    }

    @Test
    void getGroupResponseById_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupResponseById(1L));

        verify(courseGroupRepository).findByIdWithDetails(1L);
    }

    @Test
    void updateGroup_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.updateGroup(1L, updateGroupDTO));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void updateGroup_ShouldThrowResourceNotFoundException_WhenTeacherNotFound() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(teacherRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.updateGroup(1L, updateGroupDTO));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(teacherRepository).findById(2L);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void updateGroup_ShouldThrowDataIntegrityViolationException_WhenGroupNumberExists() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(testCourse, 3, 1L)).thenReturn(true);

        UpdateGroupDTO groupNumberOnlyDTO = new UpdateGroupDTO();
        groupNumberOnlyDTO.setGroupNumber(3);

        assertThrows(DataIntegrityViolationException.class, () -> groupService.updateGroup(1L, groupNumberOnlyDTO));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(courseGroupRepository).existsByCourseAndGroupNumberAndCourseGroupIdNot(testCourse, 3, 1L);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void updateGroup_ShouldThrowIllegalArgumentException_WhenNewCapacityLessThanEnrolledCount() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(40);

        UpdateGroupDTO capacityOnlyDTO = new UpdateGroupDTO();
        capacityOnlyDTO.setMaxCapacity(35);

        assertThrows(IllegalArgumentException.class, () -> groupService.updateGroup(1L, capacityOnlyDTO));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void deleteGroup_ShouldDeleteGroup_WhenGroupExists() {
        when(courseGroupRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courseGroupRepository).deleteById(1L);

        groupService.deleteGroup(1L);

        verify(courseGroupRepository).existsById(1L);
        verify(courseGroupRepository).deleteById(1L);
    }

    @Test
    void deleteGroup_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        when(courseGroupRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> groupService.deleteGroup(1L));

        verify(courseGroupRepository).existsById(1L);
        verify(courseGroupRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteGroup_ShouldThrowDeletionBlockedException_WhenDataIntegrityViolationOccurs() {
        when(courseGroupRepository.existsById(1L)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(courseGroupRepository).deleteById(1L);

        assertThrows(DeletionBlockedException.class, () -> groupService.deleteGroup(1L));

        verify(courseGroupRepository).existsById(1L);
        verify(courseGroupRepository).deleteById(1L);
    }

    @Test
    void getGroupsByCourseId_ShouldReturnListOfGroupResponses_WhenCourseExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.findByCourseWithDetails(testCourse)).thenReturn(List.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        List<GroupResponse> result = groupService.getGroupsByCourseId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGroupId());
        assertEquals(15, result.get(0).getEnrolledCount());

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).findByCourseWithDetails(testCourse);
        verify(enrollmentRepository).countByGroup(testGroup);
    }

    @Test
    void getGroupsByCourseId_ShouldThrowResourceNotFoundException_WhenCourseNotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupsByCourseId(1L));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository, never()).findByCourseWithDetails(any());
    }

    @Test
    void deleteAllGroupsByCourseId_ShouldDeleteAllGroups_WhenCourseExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.findByCourse(testCourse)).thenReturn(List.of(testGroup));
        doNothing().when(courseGroupRepository).deleteAll(anyList());

        groupService.deleteAllGroupsByCourseId(1L);

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).findByCourse(testCourse);
        verify(courseGroupRepository).deleteAll(List.of(testGroup));
    }

    @Test
    void deleteAllGroupsByCourseId_ShouldThrowResourceNotFoundException_WhenCourseNotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.deleteAllGroupsByCourseId(1L));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository, never()).findByCourse(any());
        verify(courseGroupRepository, never()).deleteAll(anyList());
    }

    @Test
    void deleteAllGroupsByCourseId_ShouldThrowDeletionBlockedException_WhenDataIntegrityViolationOccurs() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.findByCourse(testCourse)).thenReturn(List.of(testGroup));
        doThrow(DataIntegrityViolationException.class).when(courseGroupRepository).deleteAll(anyList());

        assertThrows(DeletionBlockedException.class, () -> groupService.deleteAllGroupsByCourseId(1L));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).findByCourse(testCourse);
        verify(courseGroupRepository).deleteAll(List.of(testGroup));
    }

    @Test
    void findAvailableGroupsForStudent_ShouldReturnPageOfGroupAvailabilityResponses() {
        Pageable pageable = Pageable.unpaged();
        Page<CourseGroup> groupPage = new PageImpl<>(List.of(testGroup));

        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(enrollmentRepository.findCourseIdsByStudent(testStudent)).thenReturn(List.of(2L));
        when(courseGroupRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(groupPage);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        Page<GroupAvailabilityResponse> result = groupService.findAvailableGroupsForStudent(1L, "CS", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getGroupId());
        assertEquals(15, result.getContent().get(0).getEnrolledCount());
        assertEquals(15, result.getContent().get(0).getAvailableSlots());

        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository).findCourseIdsByStudent(testStudent);
        verify(courseGroupRepository).findAll(any(Specification.class), eq(pageable));
        verify(enrollmentRepository).countByGroup(testGroup);
    }

    @Test
    void getEnrolledStudents_ShouldReturnPageOfStudentResponses_WhenGroupExists() {
        Pageable pageable = Pageable.unpaged();
        Page<Student> studentPage = new PageImpl<>(List.of(testStudent));

        when(courseGroupRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findStudentsByGroupId(1L, pageable)).thenReturn(studentPage);
        when(studentService.mapToStudentResponse(testStudent)).thenReturn(new StudentResponse());

        Page<StudentResponse> result = groupService.getEnrolledStudents(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(courseGroupRepository).existsById(1L);
        verify(enrollmentRepository).findStudentsByGroupId(1L, pageable);
        verify(studentService).mapToStudentResponse(testStudent);
    }

    @Test
    void getEnrolledStudents_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        Pageable pageable = Pageable.unpaged();
        when(courseGroupRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> groupService.getEnrolledStudents(1L, pageable));

        verify(courseGroupRepository).existsById(1L);
        verify(enrollmentRepository, never()).findStudentsByGroupId(anyLong(), any());
    }
}
