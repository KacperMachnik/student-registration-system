package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {
    private Long gradeId;
    private String gradeValue;
    private String gradeDate;
    private String comment;
    private StudentMinimalResponse student;
    private CourseMinimalResponse course;
    private TeacherMinimalResponse teacher;
}