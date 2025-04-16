package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.model.User;
import pl.edu.agh.student_registration_system.payload.dto.UpdateTeacherDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;


    @Override
    @Transactional(readOnly = true)
    public Teacher findCurrentTeacherEntity() {
        log.debug("Finding current teacher entity");
        User currentUser = userService.getCurrentAuthenticatedUser();
        return teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> {
                    log.warn("Teacher profile not found for current user {}", currentUser.getEmail());
                    return new ResourceNotFoundException("Teacher profile", "currentUser", currentUser.getEmail());
                });
    }


    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getTeacherResponseById(Long teacherId) {
        return mapToTeacherResponse(findById(teacherId));
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getCurrentTeacherResponse() {
        return mapToTeacherResponse(findCurrentTeacherEntity());
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(Long teacherId, UpdateTeacherDTO dto) {
        log.info("Attempting to update teacher with ID: {}", teacherId);
        Teacher teacher = findById(teacherId);
        User user = teacher.getUser();

        boolean changed = false;

        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new UserAlreadyExistsException("Email '" + dto.getEmail() + "' is already in use!");
            }
            user.setEmail(dto.getEmail());
            changed = true;
        }
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank() && !dto.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(dto.getFirstName());
            changed = true;
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank() && !dto.getLastName().equals(user.getLastName())) {
            user.setLastName(dto.getLastName());
            changed = true;
        }
        if (dto.getIsActive() != null && !dto.getIsActive().equals(user.getIsActive())) {
            user.setIsActive(dto.getIsActive());
            changed = true;
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank() && !dto.getTitle().equals(teacher.getTitle())) {
            teacher.setTitle(dto.getTitle());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
            if (dto.getTitle() != null) {
                teacherRepository.save(teacher);
            }
            log.info("Teacher {} and associated user {} updated.", teacherId, user.getUserId());
        } else {
            log.info("No changes detected for teacher {}", teacherId);
        }
        return mapToTeacherResponse(teacher);
    }

    @Override
    @Transactional
    public void deleteTeacherAndUser(Long teacherId) {
        log.info("Attempting delete for teacher ID: {}", teacherId);
        Teacher teacher = findById(teacherId);

        long assignedGroupsCount = courseGroupRepository.countByTeacher(teacher);
        if (assignedGroupsCount > 0) {
            log.warn("Cannot delete teacher {} because they are assigned to {} groups.", teacherId, assignedGroupsCount);
            throw new IllegalStateException("Cannot delete teacher: Teacher is assigned to active course groups.");
        }

        User userToDelete = teacher.getUser();
        if (userToDelete != null) {
            log.warn("Deleting user {} which will cascade delete teacher profile {}", userToDelete.getUserId(), teacherId);
            userRepository.delete(userToDelete);
            log.info("User {} and teacher profile {} deleted.", userToDelete.getUserId(), teacherId);
        } else {
            log.warn("Teacher {} has no associated user. Deleting teacher profile only.", teacherId);
            teacherRepository.delete(teacher);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherResponse> searchTeachers(String search, Pageable pageable) {
        log.debug("Searching teachers with query: '{}', pageable: {}", search, pageable);
        Specification<Teacher> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user").fetch("role");
            }
            query.distinct(true);

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";

            Predicate firstNameMatch = cb.like(cb.lower(root.get("user").get("firstName")), pattern);
            Predicate lastNameMatch = cb.like(cb.lower(root.get("user").get("lastName")), pattern);
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
            Predicate emailMatch = cb.like(cb.lower(root.get("user").get("email")), pattern);

            return cb.or(firstNameMatch, lastNameMatch, titleMatch, emailMatch);
        };
        Page<Teacher> page = teacherRepository.findAll(spec, pageable);
        return page.map(this::mapToTeacherResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCurrentTeacherCourses() {
        log.debug("Fetching courses taught by current teacher");
        Teacher currentTeacher = findCurrentTeacherEntity();
        List<CourseGroup> groups = courseGroupRepository.findByTeacherWithCourse(currentTeacher);
        return groups.stream()
                .map(CourseGroup::getCourse)
                .distinct()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getCurrentTeacherGroups(Long courseId) {
        log.debug("Fetching groups taught by current teacher, courseId filter: {}", courseId);
        Teacher currentTeacher = findCurrentTeacherEntity();
        List<CourseGroup> groups;
        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
            groups = courseGroupRepository.findByTeacherAndCourseWithDetails(currentTeacher, course);
        } else {
            groups = courseGroupRepository.findByTeacherWithDetails(currentTeacher);
        }
        return groups.stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Teacher findById(Long teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
    }


    private TeacherResponse mapToTeacherResponse(Teacher teacher) {
        if (teacher == null) return null;
        User user = teacher.getUser();
        if (user == null) {
            throw new IllegalStateException("Teacher (ID: " + teacher.getTeacherId() + ") is missing associated User data.");
        }

        LoginResponse userInfo = new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getIsActive(),
                List.of(user.getRole().getRoleName().name())
        );

        return new TeacherResponse(teacher.getTeacherId(), teacher.getTitle(), userInfo);
    }

    private CourseResponse mapToCourseResponse(Course course) {
        if (course == null) return null;
        return new CourseResponse(
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getDescription(),
                course.getCredits()
        );
    }

    private GroupResponse mapToGroupResponse(CourseGroup group) {
        if (group == null) return null;
        return new GroupResponse(
                group.getCourseGroupId(),
                group.getGroupNumber(),
                group.getMaxCapacity(),
                mapToCourseMinimalResponse(group.getCourse()),
                mapToTeacherMinimalResponse(group.getTeacher()),
                enrollmentRepository.countByGroup(group)
        );
    }

    private CourseMinimalResponse mapToCourseMinimalResponse(Course course) {
        if (course == null) return null;
        return new CourseMinimalResponse(
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName()
        );
    }

    private TeacherMinimalResponse mapToTeacherMinimalResponse(Teacher teacher) {
        if (teacher == null) return null;
        User user = teacher.getUser();
        return new TeacherMinimalResponse(
                teacher.getTeacherId(),
                user != null ? user.getFirstName() : null,
                user != null ? user.getLastName() : null,
                teacher.getTitle()
        );
    }
}