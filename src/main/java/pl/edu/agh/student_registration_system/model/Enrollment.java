package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "enrollments")
@Table
@Data
@ToString(exclude = {"student", "group"})
@EqualsAndHashCode(exclude = {"student", "group"})
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long enrollmentId;

    @Column(columnDefinition = "DATE")
    private LocalDateTime enrollmentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CourseGroup group;
}
