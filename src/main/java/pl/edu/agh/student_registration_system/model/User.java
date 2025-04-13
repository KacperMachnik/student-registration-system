package pl.edu.agh.student_registration_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "users")
@Data
@ToString(exclude = {"role", "studentProfile", "teacherProfile"})
@EqualsAndHashCode(exclude = {"role", "studentProfile", "teacherProfile"})
@NoArgsConstructor
@AllArgsConstructor
@Table
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Student studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Teacher teacherProfile;


}
