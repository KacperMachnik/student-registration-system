package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long groupId;
    private Integer groupNumber;
    private Integer maxCapacity;
    private CourseMinimalResponse course;
    private TeacherMinimalResponse teacher;
    private Integer enrolledCount;
}