package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAvailabilityResponse {
    private Long groupId;
    private Integer groupNumber;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private Integer availableSlots;
    private CourseResponse course;
    private TeacherMinimalResponse teacher;
}