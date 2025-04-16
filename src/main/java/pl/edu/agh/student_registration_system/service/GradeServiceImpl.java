package pl.edu.agh.student_registration_system.service;

import org.springframework.stereotype.Service;
import pl.edu.agh.student_registration_system.payload.dto.CreateGradeDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGradeDTO;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;

@Service
public class GradeServiceImpl implements GradeService {
    @Override
    public GradeResponse addGrade(CreateGradeDTO createGradeDto) {
        return null;
    }

    @Override
    public GradeResponse updateGrade(Long gradeId, UpdateGradeDTO updateGradeDto) {
        return null;
    }

    @Override
    public void deleteGrade(Long gradeId) {

    }
}
