package pl.edu.agh.student_registration_system.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "courses")
@Table
@Data
@ToString(exclude = {"courseGroups", "grades"})
@EqualsAndHashCode(exclude = {"courseGroups", "grades"})
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long courseId;

    @Column(unique = true, nullable = false)
    private String courseCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "credits")
    private Integer credits;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CourseGroup> courseGroups = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<Grade> grades = new HashSet<>();
}
