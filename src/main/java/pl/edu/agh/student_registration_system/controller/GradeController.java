package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateGradeDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGradeDTO;
import pl.edu.agh.student_registration_system.payload.response.GradeResponse;
import pl.edu.agh.student_registration_system.service.GradeService;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;


    //todo
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<GradeResponse> addGrade(@Valid @RequestBody CreateGradeDTO createGradeDto) {
        GradeResponse newGrade = gradeService.addGrade(createGradeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newGrade);
    }

    //todo
    @PutMapping("/{gradeId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<GradeResponse> updateGrade(@PathVariable Long gradeId, @Valid @RequestBody UpdateGradeDTO updateGradeDto) {
        GradeResponse updatedGrade = gradeService.updateGrade(gradeId, updateGradeDto);
        return ResponseEntity.ok(updatedGrade);
    }

    //todo
    @DeleteMapping("/{gradeId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        gradeService.deleteGrade(gradeId);
        return ResponseEntity.noContent().build();
    }
}
