package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "attendance")
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meeting_id", "student_id"}, name = "uk_meeting_student_attendance")
})
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long attendanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_teacher_id", nullable = false)
    private Teacher recordedByTeacher;
}
