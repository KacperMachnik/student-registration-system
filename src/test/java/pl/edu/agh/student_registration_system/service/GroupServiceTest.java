package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private Course course;
    private Teacher teacher;
    private CourseGroup courseGroup;
    private CreateGroupDTO createGroupDTO;
    private UpdateGroupDTO updateGroupDTO;
    private Student student;
    private User studentUser;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        course = new Course(1L, "Test Course", "TC101", "Desc", 3, null, null);
        teacherUser = new User(1L, "Teacher", "Test", "pass", "teacher@test.com", true, null, null, null);
        teacher = new Teacher(1L, "Dr", teacherUser, null, null, null);
        courseGroup = new CourseGroup(1L, 1, 30, course, teacher, null, null);

        createGroupDTO = new CreateGroupDTO(1L, 1L, 1, 30);
        updateGroupDTO = new UpdateGroupDTO(1L, 2, 25);

        studentUser = new User(2L, "Student", "Test", "pass", "student@test.com", true, null, null, null);
        student = new Student(1L, "123456", studentUser, null, null, null);
    }

    @Test
    void createGroup_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(courseGroupRepository.existsByCourseAndGroupNumber(course, 1)).thenReturn(false);
        when(courseGroupRepository.save(any(CourseGroup.class))).thenReturn(courseGroup);
        when(enrollmentRepository.countByGroup(any(CourseGroup.class))).thenReturn(0);


        GroupResponse response = groupService.createGroup(createGroupDTO);

        assertNotNull(response);
        assertEquals(createGroupDTO.getGroupNumber(), response.getGroupNumber());
        verify(courseGroupRepository).save(any(CourseGroup.class));
    }

    @Test
    void createGroup_Success_NoTeacher() {
        CreateGroupDTO createDtoNoTeacher = new CreateGroupDTO(1L, null, 1, 30);
        CourseGroup groupNoTeacher = new CourseGroup(2L, 1, 30, course, null, null, null);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseGroupRepository.existsByCourseAndGroupNumber(course, 1)).thenReturn(false);
        when(courseGroupRepository.save(any(CourseGroup.class))).thenReturn(groupNoTeacher);
        when(enrollmentRepository.countByGroup(any(CourseGroup.class))).thenReturn(0);

        GroupResponse response = groupService.createGroup(createDtoNoTeacher);

        assertNotNull(response);
        assertEquals(createDtoNoTeacher.getGroupNumber(), response.getGroupNumber());
        assertNull(response.getTeacher());
        verify(courseGroupRepository).save(any(CourseGroup.class));
    }


    @Test
    void createGroup_CourseNotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> groupService.createGroup(createGroupDTO));
    }

    @Test
    void createGroup_TeacherNotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> groupService.createGroup(createGroupDTO));
    }



    @Test
    void getGroupResponseById_Success() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(10);
        GroupResponse response = groupService.getGroupResponseById(1L);
        assertNotNull(response);
        assertEquals(courseGroup.getGroupNumber(), response.getGroupNumber());
        assertEquals(10, response.getEnrolledCount());
    }

    @Test
    void getGroupResponseById_NotFound_ThrowsResourceNotFoundException() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupResponseById(1L));
    }

    @Test
    void updateGroup_Success_AllFieldsChanged() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        Teacher newTeacher = new Teacher(2L, "Prof", new User(), null, null, null);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(newTeacher));
        when(courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course, 2, 1L)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(0);
        when(courseGroupRepository.save(any(CourseGroup.class))).thenReturn(courseGroup);

        GroupResponse response = groupService.updateGroup(1L, updateGroupDTO);

        assertNotNull(response);
        assertEquals(updateGroupDTO.getGroupNumber(), response.getGroupNumber());
        assertEquals(updateGroupDTO.getMaxCapacity(), response.getMaxCapacity());
        verify(courseGroupRepository).save(any(CourseGroup.class));
    }

    @Test
    void updateGroup_Success_RemoveTeacher() {
        UpdateGroupDTO dtoRemoveTeacher = new UpdateGroupDTO(null, 2, 25);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course, 2, 1L)).thenReturn(false);
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(0);
        when(courseGroupRepository.save(any(CourseGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupResponse response = groupService.updateGroup(1L, dtoRemoveTeacher);

        assertNotNull(response);
        assertNull(response.getTeacher());
        verify(courseGroupRepository).save(any(CourseGroup.class));
    }


    @Test
    void updateGroup_GroupNumberExists_ThrowsDataIntegrityViolationException() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        Teacher newTeacher = new Teacher(2L, "Prof", new User(), null, null, null);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(newTeacher));
        when(courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(course, 2, 1L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> groupService.updateGroup(1L, updateGroupDTO));
    }

    @Test
    void updateGroup_NewCapacityLessThanEnrolled_ThrowsIllegalArgumentException() {
        UpdateGroupDTO dtoLowCapacity = new UpdateGroupDTO(1L, 1, 5);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(10);

        assertThrows(IllegalArgumentException.class, () -> groupService.updateGroup(1L, dtoLowCapacity));
    }




    @Test
    void deleteGroup_Success() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(courseGroup));
        doNothing().when(courseGroupRepository).delete(courseGroup);
        groupService.deleteGroup(1L);
        verify(courseGroupRepository).delete(courseGroup);
    }

    @Test
    void deleteGroup_DataIntegrityViolation_ThrowsDeletionBlockedException() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(courseGroup));
        doThrow(new DataIntegrityViolationException("constraint")).when(courseGroupRepository).delete(courseGroup);
        assertThrows(DeletionBlockedException.class, () -> groupService.deleteGroup(1L));
    }


    @Test
    void getGroupsByCourseId_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseGroupRepository.findByCourseWithDetails(course)).thenReturn(Collections.singletonList(courseGroup));
        when(enrollmentRepository.countByGroup(courseGroup)).thenReturn(5);

        List<GroupResponse> responses = groupService.getGroupsByCourseId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(5, responses.get(0).getEnrolledCount());
    }

    @Test
    void deleteAllGroupsByCourseId_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        List<CourseGroup> groups = Collections.singletonList(courseGroup);
        when(courseGroupRepository.findByCourse(course)).thenReturn(groups);
        doNothing().when(courseGroupRepository).deleteAllInBatch(groups);

        groupService.deleteAllGroupsByCourseId(1L);
        verify(courseGroupRepository).deleteAllInBatch(groups);
    }

    @Test
    void deleteAllGroupsByCourseId_NoGroupsFound_DoesNothing() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseGroupRepository.findByCourse(course)).thenReturn(Collections.emptyList());

        groupService.deleteAllGroupsByCourseId(1L);
        verify(courseGroupRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAvailableGroupsForStudent_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(enrollmentRepository.findCourseIdsByStudent(student)).thenReturn(Collections.emptyList());

        CourseGroup availableGroup = new CourseGroup(2L, 2, 10, course, teacher, null, null);
        Page<CourseGroup> groupPage = new PageImpl<>(Collections.singletonList(availableGroup), pageable, 1);
        when(courseGroupRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(groupPage);
        when(enrollmentRepository.countByGroup(availableGroup)).thenReturn(5);

        Page<GroupAvailabilityResponse> responsePage = groupService.findAvailableGroupsForStudent(1L, null, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(5, responsePage.getContent().get(0).getAvailableSlots());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAvailableGroupsForStudent_FiltersOutFullGroups() {
        Pageable pageable = PageRequest.of(0, 10);
        when(studentService.findCurrentStudentEntity()).thenReturn(student);
        when(enrollmentRepository.findCourseIdsByStudent(student)).thenReturn(Collections.emptyList());

        CourseGroup fullGroup = new CourseGroup(3L, 3, 5, course, teacher, null, null);
        Page<CourseGroup> groupPage = new PageImpl<>(Collections.singletonList(fullGroup), pageable, 1);
        when(courseGroupRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(groupPage);
        when(enrollmentRepository.countByGroup(fullGroup)).thenReturn(5);

        Page<GroupAvailabilityResponse> responsePage = groupService.findAvailableGroupsForStudent(null, null, pageable);

        assertNotNull(responsePage);
        assertTrue(responsePage.getContent().isEmpty());
        assertEquals(1, responsePage.getTotalElements());
    }


    @Test
    void getEnrolledStudents_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> studentPage = new PageImpl<>(Collections.singletonList(student), pageable, 1);
        LoginResponse loginResponse = new LoginResponse(studentUser.getUserId(), studentUser.getEmail(), studentUser.getFirstName(), studentUser.getLastName(), studentUser.getIsActive(), Collections.emptyList());
        StudentResponse studentResponse = new StudentResponse(student.getStudentId(), student.getIndexNumber(), loginResponse);


        when(courseGroupRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findStudentsByGroupId(1L, pageable)).thenReturn(studentPage);
        when(studentService.mapToStudentResponse(student)).thenReturn(studentResponse);

        Page<StudentResponse> responsePage = groupService.getEnrolledStudents(1L, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(student.getStudentId(), responsePage.getContent().get(0).getStudentId());
    }

    @Test
    void getEnrolledStudents_GroupNotFound_ThrowsResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(courseGroupRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> groupService.getEnrolledStudents(1L, pageable));
    }

}