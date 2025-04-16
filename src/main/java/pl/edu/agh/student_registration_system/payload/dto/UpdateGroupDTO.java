package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupDTO {
    private Long teacherId;
    @Positive
    private Integer groupNumber;
    @Positive
    private Integer maxCapacity;
}