package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "course_groups")
@Table(name = "course_groups", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "group_number"}, name = "uk_course_group_number")
})
@Data
@ToString(exclude = {"course", "teacher", "enrollments", "meetings"})
@EqualsAndHashCode(exclude = {"course", "teacher", "enrollments", "meetings"})
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long courseGroupId;

    @Column(nullable = false)
    private Integer groupNumber;

    @Column(nullable = false)
    private Integer maxCapacity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();
}