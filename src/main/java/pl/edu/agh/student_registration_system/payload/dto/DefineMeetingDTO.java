package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefineMeetingDTO {
    @NotNull
    @Min(1)
    private Integer numberOfMeetings;

    @NotNull
    private LocalDateTime firstMeetingDateTime;

    private String topic;
}