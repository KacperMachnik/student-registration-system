package pl.edu.agh.student_registration_system.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "roles")
@Table
@Data
@ToString(exclude = {"users"})
@EqualsAndHashCode(exclude = {"users"})
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType roleName;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    public Role(RoleType roleName) {
        this.roleName = roleName;
    }
}
