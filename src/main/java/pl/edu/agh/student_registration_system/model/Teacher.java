package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "teachers")
@Table
@Data
@ToString(exclude = {"taughtGroups", "issuedGrades", "recordedAttendances"})
@EqualsAndHashCode(exclude = {"taughtGroups", "issuedGrades", "recordedAttendances"})
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long teacherId;

    private String title;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<CourseGroup> taughtGroups = new HashSet<>();

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<Grade> issuedGrades = new HashSet<>();

    @OneToMany(mappedBy = "recordedByTeacher", fetch = FetchType.LAZY)
    private Set<Attendance> recordedAttendances = new HashSet<>();
}
