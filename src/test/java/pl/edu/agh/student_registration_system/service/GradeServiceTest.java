package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.payload.dto.CreateGradeDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGradeDTO;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @InjectMocks
    private GradeServiceImpl gradeService;

    private CreateGradeDTO createGradeDTO;
    private UpdateGradeDTO updateGradeDTO;

    @BeforeEach
    void setUp() {
        createGradeDTO = new CreateGradeDTO();
        updateGradeDTO = new UpdateGradeDTO();
    }

    @Test
    void addGrade_ShouldReturnNull() {
        GradeResponse result = gradeService.addGrade(createGradeDTO);

        assertNull(result);
    }

    @Test
    void updateGrade_ShouldReturnNull() {
        Long gradeId = 1L;

        GradeResponse result = gradeService.updateGrade(gradeId, updateGradeDTO);

        assertNull(result);
    }

    @Test
    void deleteGrade_ShouldNotThrowException() {
        Long gradeId = 1L;

        assertDoesNotThrow(() -> gradeService.deleteGrade(gradeId));
    }
}
