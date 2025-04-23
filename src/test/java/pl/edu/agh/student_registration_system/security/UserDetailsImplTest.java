package pl.edu.agh.student_registration_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.edu.agh.student_registration_system.model.Role;
import pl.edu.agh.student_registration_system.model.RoleType;
import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.security.service.UserDetailsImpl;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void shouldBuildUserDetailsFromUser() {
        Long userId = 1L;
        String email = "test@example.com";
        String password = "password";

        Role role = new Role();
        role.setRoleName(RoleType.STUDENT);

        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        assertEquals(userId, userDetails.getId());
        assertEquals(email, userDetails.getEmail());
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());

        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();
        assertEquals("STUDENT", authority.getAuthority());
    }

    @Test
    void shouldCreateUserDetailsWithAllArgs() {
        Long id = 1L;
        String email = "test@example.com";
        String password = "password";
        Collection<GrantedAuthority> authorities =
                java.util.Collections.singletonList(new SimpleGrantedAuthority("TEACHER"));

        UserDetailsImpl userDetails = new UserDetailsImpl(id, email, password, authorities);

        assertEquals(id, userDetails.getId());
        assertEquals(email, userDetails.getEmail());
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(authorities, userDetails.getAuthorities());
    }

    @Test
    void shouldImplementUserDetailsInterface() {
        Long id = 1L;
        String email = "test@example.com";
        String password = "password";
        Collection<GrantedAuthority> authorities =
                java.util.Collections.singletonList(new SimpleGrantedAuthority("TEACHER"));

        UserDetailsImpl userDetails = new UserDetailsImpl(id, email, password, authorities);

        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(authorities, userDetails.getAuthorities());
    }
}
