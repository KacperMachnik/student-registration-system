package pl.edu.agh.student_registration_system.service;

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
    private Teacher testTeacher2;
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


        Role teacherRole = new Role(RoleType.TEACHER);
        teacherRole.setRoleId(1L);
        testUser.setRole(teacherRole);


        testTeacher = new Teacher();
        testTeacher.setTeacherId(1L);
        testTeacher.setTitle("Professor");
        testTeacher.setUser(testUser);

        User testUser2 = new User();
        testUser2.setUserId(2L);
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setRole(teacherRole);


        testTeacher2 = new Teacher();
        testTeacher2.setTeacherId(2L);
        testTeacher2.setTitle("Associate Professor");
        testTeacher2.setUser(testUser2);

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
        User studentUser = new User();
        studentUser.setUserId(3L);
        studentUser.setFirstName("Alice");
        studentUser.setLastName("Student");
        Role studentRole = new Role(RoleType.STUDENT);
        studentRole.setRoleId(2L);
        studentUser.setRole(studentRole);
        testStudent.setUser(studentUser);


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
        assertEquals(createGroupDTO.getGroupNumber(), result.getGroupNumber());
        assertEquals(createGroupDTO.getMaxCapacity(), result.getMaxCapacity());
        assertEquals(0, result.getEnrolledCount());
        assertNotNull(result.getCourse());
        assertEquals(testCourse.getCourseId(), result.getCourse().getCourseId());
        assertNotNull(result.getTeacher());
        assertEquals(testTeacher.getTeacherId(), result.getTeacher().getTeacherId());

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).existsByCourseAndGroupNumber(testCourse, createGroupDTO.getGroupNumber());
        verify(teacherRepository).findById(1L);
        verify(courseGroupRepository).save(courseGroupCaptor.capture());

        CourseGroup capturedGroup = courseGroupCaptor.getValue();
        assertEquals(testCourse, capturedGroup.getCourse());
        assertEquals(testTeacher, capturedGroup.getTeacher());
        assertEquals(createGroupDTO.getGroupNumber(), capturedGroup.getGroupNumber());
        assertEquals(createGroupDTO.getMaxCapacity(), capturedGroup.getMaxCapacity());
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
        when(courseGroupRepository.existsByCourseAndGroupNumber(testCourse, createGroupDTO.getGroupNumber())).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> groupService.createGroup(createGroupDTO));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).existsByCourseAndGroupNumber(testCourse, createGroupDTO.getGroupNumber());
        verify(teacherRepository, never()).findById(anyLong());
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void getGroupResponseById_ShouldReturnGroupResponse_WhenGroupExists() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);

        GroupResponse result = groupService.getGroupResponseById(1L);

        assertNotNull(result);
        assertEquals(testGroup.getCourseGroupId(), result.getGroupId());
        assertEquals(testGroup.getGroupNumber(), result.getGroupNumber());
        assertEquals(testGroup.getMaxCapacity(), result.getMaxCapacity());
        assertEquals(15, result.getEnrolledCount());
        assertNotNull(result.getCourse());
        assertEquals(testCourse.getCourseId(), result.getCourse().getCourseId());
        assertNotNull(result.getTeacher());
        assertEquals(testTeacher.getTeacherId(), result.getTeacher().getTeacherId());

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
    void updateGroup_ShouldUpdateGroupSuccessfully() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(testTeacher2));
        when(courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(testCourse, 3, 1L)).thenReturn(false);
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(20);
        when(courseGroupRepository.save(any(CourseGroup.class))).thenReturn(testGroup);

        GroupResponse result = groupService.updateGroup(1L, updateGroupDTO);

        assertNotNull(result);
        verify(courseGroupRepository).save(courseGroupCaptor.capture());
        CourseGroup captured = courseGroupCaptor.getValue();

        assertEquals(testTeacher2, captured.getTeacher());
        assertEquals(updateGroupDTO.getGroupNumber(), captured.getGroupNumber());
        assertEquals(updateGroupDTO.getMaxCapacity(), captured.getMaxCapacity());
    }


    @Test
    void updateGroup_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.updateGroup(1L, updateGroupDTO));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void updateGroup_ShouldThrowResourceNotFoundException_WhenTeacherNotFoundForUpdate() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        UpdateGroupDTO dtoWithTeacher = new UpdateGroupDTO();
        dtoWithTeacher.setTeacherId(99L);
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.updateGroup(1L, dtoWithTeacher));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(teacherRepository).findById(99L);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void updateGroup_ShouldThrowDataIntegrityViolationException_WhenUpdatedGroupNumberExistsForCourse() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        UpdateGroupDTO dtoWithExistingNumber = new UpdateGroupDTO();
        dtoWithExistingNumber.setGroupNumber(5);
        when(courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(testGroup.getCourse(), 5, 1L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> groupService.updateGroup(1L, dtoWithExistingNumber));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(courseGroupRepository).existsByCourseAndGroupNumberAndCourseGroupIdNot(testGroup.getCourse(), 5, 1L);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void updateGroup_ShouldThrowIllegalArgumentException_WhenNewCapacityLessThanEnrolledCount() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testGroup));
        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(20);

        UpdateGroupDTO dtoWithLowerCapacity = new UpdateGroupDTO();
        dtoWithLowerCapacity.setMaxCapacity(15);

        assertThrows(IllegalArgumentException.class, () -> groupService.updateGroup(1L, dtoWithLowerCapacity));

        verify(courseGroupRepository).findByIdWithDetails(1L);
        verify(enrollmentRepository).countByGroup(testGroup);
        verify(courseGroupRepository, never()).save(any());
    }

    @Test
    void deleteGroup_ShouldDeleteGroup_WhenGroupExists() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        doNothing().when(courseGroupRepository).delete(any(CourseGroup.class));

        groupService.deleteGroup(1L);

        verify(courseGroupRepository).findById(1L);
        verify(courseGroupRepository).delete(testGroup);
    }

    @Test
    void deleteGroup_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> groupService.deleteGroup(1L));

        verify(courseGroupRepository).findById(1L);
        verify(courseGroupRepository, never()).delete(any(CourseGroup.class));
    }

    @Test
    void deleteGroup_ShouldThrowDeletionBlockedException_WhenDataIntegrityViolationOccurs() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        doThrow(new DataIntegrityViolationException("Simulated DIVE for delete")).when(courseGroupRepository).delete(any(CourseGroup.class));

        assertThrows(DeletionBlockedException.class, () -> groupService.deleteGroup(1L));

        verify(courseGroupRepository).findById(1L);
        verify(courseGroupRepository).delete(testGroup);
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
        doNothing().when(courseGroupRepository).deleteAllInBatch(anyList());

        groupService.deleteAllGroupsByCourseId(1L);

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).findByCourse(testCourse);
        verify(courseGroupRepository).deleteAllInBatch(List.of(testGroup));
    }

    @Test
    void deleteAllGroupsByCourseId_ShouldThrowResourceNotFoundException_WhenCourseNotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.deleteAllGroupsByCourseId(1L));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository, never()).findByCourse(any());
        verify(courseGroupRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    void deleteAllGroupsByCourseId_ShouldThrowDeletionBlockedException_WhenDataIntegrityViolationOccurs() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseGroupRepository.findByCourse(testCourse)).thenReturn(List.of(testGroup));
        doThrow(new DataIntegrityViolationException("Simulated DIVE")).when(courseGroupRepository).deleteAllInBatch(anyList());

        assertThrows(DeletionBlockedException.class, () -> groupService.deleteAllGroupsByCourseId(1L));

        verify(courseRepository).findById(1L);
        verify(courseGroupRepository).findByCourse(testCourse);
        verify(courseGroupRepository).deleteAllInBatch(List.of(testGroup));
    }

    @Test
    void findAvailableGroupsForStudent_ShouldReturnPageOfGroupAvailabilityResponses() {
        Pageable pageable = Pageable.unpaged();
        CourseGroup group2 = new CourseGroup();
        group2.setCourseGroupId(2L);
        group2.setCourse(testCourse);
        group2.setGroupNumber(2);
        group2.setMaxCapacity(20);


        List<CourseGroup> queriedGroups = List.of(testGroup, group2);
        Page<CourseGroup> groupPage = new PageImpl<>(queriedGroups, pageable, queriedGroups.size());

        when(studentService.findCurrentStudentEntity()).thenReturn(testStudent);
        when(enrollmentRepository.findCourseIdsByStudent(testStudent)).thenReturn(List.of(99L));
        when(courseGroupRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(groupPage);


        when(enrollmentRepository.countByGroup(testGroup)).thenReturn(15);
        when(enrollmentRepository.countByGroup(group2)).thenReturn(20);



        Page<GroupAvailabilityResponse> result = groupService.findAvailableGroupsForStudent(null, null, pageable);

        assertNotNull(result);

        assertEquals(1, result.getContent().size());
        assertEquals(queriedGroups.size(), result.getTotalElements());
        GroupAvailabilityResponse availableGroup = result.getContent().get(0);
        assertEquals(testGroup.getCourseGroupId(), availableGroup.getGroupId());
        assertEquals(15, availableGroup.getEnrolledCount());
        assertEquals(15, availableGroup.getAvailableSlots());

        verify(studentService).findCurrentStudentEntity();
        verify(enrollmentRepository).findCourseIdsByStudent(testStudent);
        verify(courseGroupRepository).findAll(any(Specification.class), eq(pageable));
        verify(enrollmentRepository, times(2)).countByGroup(any(CourseGroup.class));
    }


    @Test
    void getEnrolledStudents_ShouldReturnPageOfStudentResponses_WhenGroupExists() {
        Pageable pageable = Pageable.unpaged();
        Page<Student> studentPage = new PageImpl<>(List.of(testStudent));


        StudentResponse studentResponse = new StudentResponse(
                testStudent.getStudentId(),
                testStudent.getIndexNumber(),
                new LoginResponse(testStudent.getUser().getUserId(), testStudent.getUser().getEmail(), testStudent.getUser().getFirstName(), testStudent.getUser().getLastName(), true, List.of("STUDENT"))
        );


        when(courseGroupRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findStudentsByGroupId(1L, pageable)).thenReturn(studentPage);
        when(studentService.mapToStudentResponse(testStudent)).thenReturn(studentResponse);

        Page<StudentResponse> result = groupService.getEnrolledStudents(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(studentResponse, result.getContent().get(0));


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