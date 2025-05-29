package pl.edu.agh.student_registration_system.security;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.repository.RoleRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;
import pl.edu.agh.student_registration_system.repository.UserRepository;
import pl.edu.agh.student_registration_system.security.jwt.AuthEntryPointJwt;
import pl.edu.agh.student_registration_system.security.jwt.AuthTokenFilter;
import pl.edu.agh.student_registration_system.security.jwt.JwtUtils;
import pl.edu.agh.student_registration_system.security.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final StudentRepository studentRepository;


    @Autowired
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService,
                             AuthEntryPointJwt unauthorizedHandler,
                             JwtUtils jwtUtils,
                             StudentRepository studentRepository) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
        this.studentRepository = studentRepository;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);  // Important for cookies/JWT headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/api/users/register/**").hasAuthority(RoleType.DEANERY_STAFF.name())

                        .requestMatchers("/api/students/me/**").hasAuthority(RoleType.STUDENT.name())
                        .requestMatchers(HttpMethod.POST, "/api/enrollments/my/**").hasAuthority(RoleType.STUDENT.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/enrollments/my/**").hasAuthority(RoleType.STUDENT.name())
                        .requestMatchers(HttpMethod.GET, "/api/groups/available").hasAuthority(RoleType.STUDENT.name())

                        .requestMatchers("/api/teachers/me/**").hasAuthority(RoleType.TEACHER.name())
                        .requestMatchers(HttpMethod.POST, "/api/grades").hasAuthority(RoleType.TEACHER.name())
                        .requestMatchers(HttpMethod.PUT, "/api/grades/**").hasAnyAuthority(RoleType.TEACHER.name(), RoleType.DEANERY_STAFF.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/grades/**").hasAnyAuthority(RoleType.TEACHER.name(), RoleType.DEANERY_STAFF.name())


                        .requestMatchers(HttpMethod.GET, "/api/groups/{groupId}/students").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/groups/{groupId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/groups/{groupId}/meetings").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/groups/{groupId}/meetings").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/meetings/{meetingId}/attendance").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/meetings/{meetingId}/attendance").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/attendance/{attendanceId}").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/courses/{courseId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/courses/{courseId}/groups").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/students/{studentId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/teachers/{teacherId}").authenticated()



                        .requestMatchers("/api/students/**").hasAuthority(RoleType.DEANERY_STAFF.name())
                        .requestMatchers("/api/teachers/**").hasAuthority(RoleType.DEANERY_STAFF.name())
                        .requestMatchers("/api/courses/**").hasAuthority(RoleType.DEANERY_STAFF.name())
                        .requestMatchers("/api/groups/**").hasAuthority(RoleType.DEANERY_STAFF.name())
                        .requestMatchers(HttpMethod.POST, "/api/enrollments/admin/**").hasAuthority(RoleType.DEANERY_STAFF.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/enrollments/admin/**").hasAuthority(RoleType.DEANERY_STAFF.name())


                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()

                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }


    @Bean
    CommandLineRunner initData(RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("--- Data Initializer Start ---");

            log.info("Checking and initializing roles...");
            Role studentRole = roleRepository.findByRoleName(RoleType.STUDENT)
                    .orElseGet(() -> {
                        log.info("Creating STUDENT role.");
                        return roleRepository.save(new Role(RoleType.STUDENT));
                    });
            Role teacherRole = roleRepository.findByRoleName(RoleType.TEACHER)
                    .orElseGet(() -> {
                        log.info("Creating TEACHER role.");
                        return roleRepository.save(new Role(RoleType.TEACHER));
                    });
            Role deaneryRole = roleRepository.findByRoleName(RoleType.DEANERY_STAFF)
                    .orElseGet(() -> {
                        log.info("Creating DEANERY_STAFF role.");
                        return roleRepository.save(new Role(RoleType.DEANERY_STAFF));
                    });
            log.info("Roles initialized.");

            String adminEmail = "admin@university.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User adminUser = new User();
                adminUser.setEmail(adminEmail);
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setFirstName("Admin");
                adminUser.setLastName("User");
                adminUser.setRole(deaneryRole);
                adminUser.setIsActive(true);
                userRepository.save(adminUser);
                log.info("Default DEANERY_STAFF user created: {}", adminEmail);
            } else {
                log.info("Default DEANERY_STAFF user with email '{}' already exists.", adminEmail);
            }

            String teacherEmail = "teacher@university.com";
            if (!userRepository.existsByEmail(teacherEmail)) {
                User teacherUser = new User();
                teacherUser.setEmail(teacherEmail);
                teacherUser.setPassword(passwordEncoder.encode("teacher123"));
                teacherUser.setFirstName("Jan");
                teacherUser.setLastName("Kowalski");
                teacherUser.setRole(teacherRole);
                teacherUser.setIsActive(true);

                Teacher teacherProfile = new Teacher();
                teacherProfile.setTitle("dr");
                teacherProfile.setUser(teacherUser);
                teacherUser.setTeacherProfile(teacherProfile);

                userRepository.save(teacherUser);
                log.info("Default TEACHER user created: {}", teacherEmail);
            } else {
                log.info("Default TEACHER user with email '{}' already exists.", teacherEmail);
            }

            String studentEmail = "student@university.com";
            if (!userRepository.existsByEmail(studentEmail)) {
                User studentUser = new User();
                studentUser.setEmail(studentEmail);
                studentUser.setPassword(passwordEncoder.encode("student123"));
                studentUser.setFirstName("Anna");
                studentUser.setLastName("Nowak");
                studentUser.setRole(studentRole);
                studentUser.setIsActive(true);

                Student studentProfile = new Student();
                String defaultIndex = "123456";
                if (this.studentRepository.existsByIndexNumber(defaultIndex)) {
                    log.warn("Default student index {} already exists! The default student might not be created correctly if index must be unique.", defaultIndex);
                }
                studentProfile.setIndexNumber(defaultIndex);
                studentProfile.setUser(studentUser);
                studentUser.setStudentProfile(studentProfile);

                userRepository.save(studentUser);
                log.info("Default STUDENT user created: {} with index {}", studentEmail, studentProfile.getIndexNumber());
            } else {
                log.info("Default STUDENT user with email '{}' already exists.", studentEmail);
            }
            log.info("--- Data Initializer End ---");
        };
    }
}