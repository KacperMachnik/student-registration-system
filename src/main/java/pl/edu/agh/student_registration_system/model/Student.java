package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "students")
@Table
@Data
@ToString(exclude = {"user", "enrollments", "grades", "attendanceRecords"})
@EqualsAndHashCode(exclude = {"user", "enrollments", "grades", "attendanceRecords"})
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long studentId;

    @Column(unique = true, nullable = false, length = 6)
    private String indexNumber;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Grade> grades = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Attendance> attendanceRecords = new HashSet<>();
}
