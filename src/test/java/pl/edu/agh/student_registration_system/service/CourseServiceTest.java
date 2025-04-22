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
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.payload.dto.CreateCourseDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateCourseDTO;
import pl.edu.agh.student_registration_system.payload.response.CourseResponse;
import pl.edu.agh.student_registration_system.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Captor
    private ArgumentCaptor<Course> courseCaptor;

    private Course testCourse;
    private CreateCourseDTO createCourseDTO;
    private UpdateCourseDTO updateCourseDTO;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setCourseId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");
        testCourse.setDescription("Basic concepts of computer science");
        testCourse.setCredits(5);

        createCourseDTO = new CreateCourseDTO();
        createCourseDTO.setCourseCode("CS102");
        createCourseDTO.setCourseName("Data Structures");
        createCourseDTO.setDescription("Study of data structures");
        createCourseDTO.setCredits(6);

        updateCourseDTO = new UpdateCourseDTO();
        updateCourseDTO.setCourseCode("CS101-Updated");
        updateCourseDTO.setCourseName("Updated Course Name");
        updateCourseDTO.setDescription("Updated description");
        updateCourseDTO.setCredits(7);
    }

    @Test
    void searchCourses_ShouldReturnPageOfCourseResponses() {
        Pageable pageable = Pageable.unpaged();
        Page<Course> coursePage = new PageImpl<>(List.of(testCourse));

        when(courseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(coursePage);

        Page<CourseResponse> result = courseService.searchCourses("CS", 1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCourse.getCourseId(), result.getContent().get(0).getCourseId());
        assertEquals(testCourse.getCourseCode(), result.getContent().get(0).getCourseCode());

        verify(courseRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenCourseExists() {
        when(courseRepository.existsById(1L)).thenReturn(true);

        boolean result = courseService.existsById(1L);

        assertTrue(result);
        verify(courseRepository).existsById(1L);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenCourseDoesNotExist() {
        when(courseRepository.existsById(1L)).thenReturn(false);

        boolean result = courseService.existsById(1L);

        assertFalse(result);
        verify(courseRepository).existsById(1L);
    }

    @Test
    void createCourse_ShouldReturnCourseResponse_WhenCourseCodeIsUnique() {
        when(courseRepository.existsByCourseCode(createCourseDTO.getCourseCode())).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setCourseId(2L);
            return savedCourse;
        });

        CourseResponse result = courseService.createCourse(createCourseDTO);

        assertNotNull(result);
        assertEquals(2L, result.getCourseId());
        assertEquals(createCourseDTO.getCourseCode(), result.getCourseCode());
        assertEquals(createCourseDTO.getCourseName(), result.getCourseName());
        assertEquals(createCourseDTO.getDescription(), result.getDescription());
        assertEquals(createCourseDTO.getCredits(), result.getCredits());

        verify(courseRepository).existsByCourseCode(createCourseDTO.getCourseCode());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_ShouldThrowDataIntegrityViolationException_WhenCourseCodeExists() {
        when(courseRepository.existsByCourseCode(createCourseDTO.getCourseCode())).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> courseService.createCourse(createCourseDTO));

        verify(courseRepository).existsByCourseCode(createCourseDTO.getCourseCode());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void getCourseResponseById_ShouldReturnCourseResponse_WhenCourseExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        CourseResponse result = courseService.getCourseResponseById(1L);

        assertNotNull(result);
        assertEquals(testCourse.getCourseId(), result.getCourseId());
        assertEquals(testCourse.getCourseCode(), result.getCourseCode());
        assertEquals(testCourse.getCourseName(), result.getCourseName());
        assertEquals(testCourse.getDescription(), result.getDescription());
        assertEquals(testCourse.getCredits(), result.getCredits());

        verify(courseRepository).findById(1L);
    }

    @Test
    void getCourseResponseById_ShouldThrowResourceNotFoundException_WhenCourseDoesNotExist() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseResponseById(1L));

        verify(courseRepository).findById(1L);
    }

    @Test
    void updateCourse_ShouldReturnUpdatedCourseResponse_WhenAllFieldsAreUpdated() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCourseCodeAndCourseIdNot(updateCourseDTO.getCourseCode(), 1L)).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        CourseResponse result = courseService.updateCourse(1L, updateCourseDTO);

        assertNotNull(result);
        verify(courseRepository).findById(1L);
        verify(courseRepository).existsByCourseCodeAndCourseIdNot(updateCourseDTO.getCourseCode(), 1L);
        verify(courseRepository).save(courseCaptor.capture());

        Course capturedCourse = courseCaptor.getValue();
        assertEquals(updateCourseDTO.getCourseCode(), capturedCourse.getCourseCode());
        assertEquals(updateCourseDTO.getCourseName(), capturedCourse.getCourseName());
        assertEquals(updateCourseDTO.getDescription(), capturedCourse.getDescription());
        assertEquals(updateCourseDTO.getCredits(), capturedCourse.getCredits());
    }

    @Test
    void updateCourse_ShouldThrowResourceNotFoundException_WhenCourseDoesNotExist() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(1L, updateCourseDTO));

        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateCourse_ShouldThrowDataIntegrityViolationException_WhenCourseCodeExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCourseCodeAndCourseIdNot(updateCourseDTO.getCourseCode(), 1L)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> courseService.updateCourse(1L, updateCourseDTO));

        verify(courseRepository).findById(1L);
        verify(courseRepository).existsByCourseCodeAndCourseIdNot(updateCourseDTO.getCourseCode(), 1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateCourse_ShouldNotSaveCourse_WhenNoChangesDetected() {
        UpdateCourseDTO noChangeDTO = new UpdateCourseDTO();
        noChangeDTO.setCourseCode(testCourse.getCourseCode());
        noChangeDTO.setCourseName(testCourse.getCourseName());
        noChangeDTO.setDescription(testCourse.getDescription());
        noChangeDTO.setCredits(testCourse.getCredits());

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        CourseResponse result = courseService.updateCourse(1L, noChangeDTO);

        assertNotNull(result);
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_ShouldDeleteCourse_WhenCourseExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        doNothing().when(courseRepository).delete(testCourse);

        courseService.deleteCourse(1L);

        verify(courseRepository).findById(1L);
        verify(courseRepository).delete(testCourse);
    }

    @Test
    void deleteCourse_ShouldThrowResourceNotFoundException_WhenCourseDoesNotExist() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(1L));

        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void deleteCourse_ShouldThrowDeletionBlockedException_WhenDataIntegrityViolationOccurs() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        doThrow(DataIntegrityViolationException.class).when(courseRepository).delete(testCourse);

        assertThrows(DeletionBlockedException.class, () -> courseService.deleteCourse(1L));

        verify(courseRepository).findById(1L);
        verify(courseRepository).delete(testCourse);
    }
}
