package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupDTO {
    @NotNull
    private Long courseId;
    private Long teacherId;
    @NotNull
    private Integer groupNumber;
    @NotNull
    private Integer maxCapacity;
}