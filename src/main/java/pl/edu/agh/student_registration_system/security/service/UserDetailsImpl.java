package pl.edu.agh.student_registration_system.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.edu.agh.student_registration_system.model.User;

import java.util.Collection;
import java.util.Collections;


@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    @JsonIgnore
    private final String email;

    @JsonIgnore
    private final String password;

    @JsonIgnore
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleName().name());
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(authority);

        return new UserDetailsImpl(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public String getUsername() {
        return email;
    }
}