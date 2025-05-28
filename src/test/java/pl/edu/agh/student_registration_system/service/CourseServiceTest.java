package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateCourseDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.repository.CourseRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course1;
    private CreateCourseDTO createCourseDTO;
    private UpdateCourseDTO updateCourseDTO;

    @BeforeEach
    void setUp() {
        course1 = new Course(1L, "Introduction to Programming", "CS101", "Basic programming concepts", 5, Collections.emptySet(), Collections.emptySet());
        createCourseDTO = new CreateCourseDTO("CS101", "Intro to Programming", "Desc", 5);
        updateCourseDTO = new UpdateCourseDTO("CS102", "Advanced Programming", "New Desc", 6);
    }

    @Test
    void searchCourses_ReturnsPagedCourseResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courses = Collections.singletonList(course1);
        Page<Course> coursePage = new PageImpl<>(courses, pageable, 1);

        when(courseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(coursePage);

        Page<CourseResponse> result = courseService.searchCourses("CS101", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CS101", result.getContent().get(0).getCourseCode());
        verify(courseRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void existsById_WhenCourseExists_ReturnsTrue() {
        when(courseRepository.existsById(1L)).thenReturn(true);
        assertTrue(courseService.existsById(1L));
        verify(courseRepository).existsById(1L);
    }

    @Test
    void existsById_WhenCourseDoesNotExist_ReturnsFalse() {
        when(courseRepository.existsById(1L)).thenReturn(false);
        assertFalse(courseService.existsById(1L));
        verify(courseRepository).existsById(1L);
    }

    @Test
    void createCourse_WhenCodeIsUnique_ReturnsCourseResponse() {
        when(courseRepository.existsByCourseCode(createCourseDTO.getCourseCode())).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course c = invocation.getArgument(0);
            c.setCourseId(1L);
            return c;
        });

        CourseResponse response = courseService.createCourse(createCourseDTO);

        assertNotNull(response);
        assertEquals(createCourseDTO.getCourseCode(), response.getCourseCode());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_WhenCodeIsNotUnique_ThrowsDataIntegrityViolationException() {
        when(courseRepository.existsByCourseCode(createCourseDTO.getCourseCode())).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> courseService.createCourse(createCourseDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void getCourseResponseById_WhenCourseExists_ReturnsCourseResponse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        CourseResponse response = courseService.getCourseResponseById(1L);
        assertNotNull(response);
        assertEquals(course1.getCourseCode(), response.getCourseCode());
        verify(courseRepository).findById(1L);
    }

    @Test
    void getCourseResponseById_WhenCourseDoesNotExist_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseResponseById(1L));
        verify(courseRepository).findById(1L);
    }

    @Test
    void updateCourse_WhenCourseExistsAndCodeIsUnique_UpdatesAndReturnsCourseResponse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.existsByCourseCodeAndCourseIdNot(updateCourseDTO.getCourseCode(), 1L)).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.updateCourse(1L, updateCourseDTO);

        assertNotNull(response);
        assertEquals(updateCourseDTO.getCourseCode(), response.getCourseCode());
        assertEquals(updateCourseDTO.getCourseName(), response.getCourseName());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_WhenCourseExistsAndNewCodeIsNotUnique_ThrowsDataIntegrityViolationException() {
        Course existingCourseWithSameCode = new Course(2L, "CS102", "Other Course", "Other Desc", 3, Collections.emptySet(), Collections.emptySet());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.existsByCourseCodeAndCourseIdNot(updateCourseDTO.getCourseCode(), 1L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> courseService.updateCourse(1L, updateCourseDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateCourse_WhenNoChanges_ReturnsOriginalCourseResponse() {
        UpdateCourseDTO noChangeDto = new UpdateCourseDTO(course1.getCourseCode(), course1.getCourseName(), course1.getDescription(), course1.getCredits());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));

        CourseResponse response = courseService.updateCourse(1L, noChangeDto);

        assertNotNull(response);
        assertEquals(course1.getCourseCode(), response.getCourseCode());
        verify(courseRepository, never()).save(any(Course.class));
    }


    @Test
    void updateCourse_WhenCourseDoesNotExist_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(1L, updateCourseDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_WhenCourseExists_DeletesCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        doNothing().when(courseRepository).delete(course1);

        courseService.deleteCourse(1L);

        verify(courseRepository).delete(course1);
    }

    @Test
    void deleteCourse_WhenCourseDoesNotExist_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void deleteCourse_WhenDataIntegrityViolation_ThrowsDeletionBlockedException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        doThrow(new DataIntegrityViolationException("constraint violation")).when(courseRepository).delete(course1);

        assertThrows(DeletionBlockedException.class, () -> courseService.deleteCourse(1L));
        verify(courseRepository).delete(course1);
    }
}