package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "meetings")
@Table(name = "meetings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "meeting_number"}, name = "uk_group_meeting_number")
})
@Data
@ToString(exclude = {"group", "attendanceRecords"})
@EqualsAndHashCode(exclude = {"group", "attendanceRecords"})
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long meetingId;

    @Column(nullable = false)
    private Integer meetingNumber;

    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate;

    private String topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CourseGroup group;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Attendance> attendanceRecords = new HashSet<>();
}