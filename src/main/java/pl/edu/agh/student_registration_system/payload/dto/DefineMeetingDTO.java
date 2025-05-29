package pl.edu.agh.student_registration_system.payload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefineMeetingDTO {
    @NotNull
    @Min(1)
    private Integer numberOfMeetings;

    @NotNull
    private LocalDateTime firstMeetingDateTime;

    @Size(max = 100)
    private List<String> topics;


    // previous impl fallback
    private String topic;
}