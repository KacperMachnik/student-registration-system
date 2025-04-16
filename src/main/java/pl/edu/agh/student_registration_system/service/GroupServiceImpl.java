package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.DeletionBlockedException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGroupDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.CourseRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.repository.TeacherRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final CourseGroupRepository courseGroupRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;


    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupDTO createGroupDto) {
        log.info("Attempting to create a new group for course ID: {}", createGroupDto.getCourseId());

        Course course = courseRepository.findById(createGroupDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", createGroupDto.getCourseId()));

        if (courseGroupRepository.existsByCourseAndGroupNumber(course, createGroupDto.getGroupNumber())) {
            throw new DataIntegrityViolationException(
                    "Group number " + createGroupDto.getGroupNumber() + " already exists for course " + course.getCourseCode());
        }

        Teacher teacher = null;
        if (createGroupDto.getTeacherId() != null) {
            teacher = teacherRepository.findById(createGroupDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", createGroupDto.getTeacherId()));
        }

        CourseGroup newGroup = new CourseGroup();
        newGroup.setCourse(course);
        newGroup.setTeacher(teacher);
        newGroup.setGroupNumber(createGroupDto.getGroupNumber());
        newGroup.setMaxCapacity(createGroupDto.getMaxCapacity());

        CourseGroup savedGroup = courseGroupRepository.save(newGroup);
        log.info("Course group created successfully with ID: {} for course: {}", savedGroup.getCourseGroupId(), course.getCourseCode());

        return mapToGroupResponse(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupResponseById(Long groupId) {
        log.debug("Fetching group with ID: {}", groupId);
        CourseGroup group = courseGroupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseGroup", "id", groupId));
        return mapToGroupResponse(group);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(Long groupId, UpdateGroupDTO updateGroupDto) {
        log.info("Attempting to update group with ID: {}", groupId);
        CourseGroup group = courseGroupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseGroup", "id", groupId));

        boolean changed = false;

        if (updateGroupDto.getTeacherId() != null) {
            Teacher newTeacher = teacherRepository.findById(updateGroupDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", updateGroupDto.getTeacherId()));
            if (!newTeacher.equals(group.getTeacher())) {
                group.setTeacher(newTeacher);
                changed = true;
                log.debug("Updated teacher for group {}", groupId);
            }
        }

        if (updateGroupDto.getGroupNumber() != null && !updateGroupDto.getGroupNumber().equals(group.getGroupNumber())) {
            if (courseGroupRepository.existsByCourseAndGroupNumberAndCourseGroupIdNot(
                    group.getCourse(), updateGroupDto.getGroupNumber(), groupId)) {
                throw new DataIntegrityViolationException(
                        "Group number " + updateGroupDto.getGroupNumber() + " already exists for course " + group.getCourse().getCourseCode());
            }
            group.setGroupNumber(updateGroupDto.getGroupNumber());
            changed = true;
            log.debug("Updated group number for group {}", groupId);
        }

        if (updateGroupDto.getMaxCapacity() != null && !updateGroupDto.getMaxCapacity().equals(group.getMaxCapacity())) {
            int currentEnrollmentCount = enrollmentRepository.countByGroup(group);
            if (updateGroupDto.getMaxCapacity() < currentEnrollmentCount) {
                throw new IllegalArgumentException("New capacity (" + updateGroupDto.getMaxCapacity()
                        + ") cannot be less than current enrollment count (" + currentEnrollmentCount + ").");
            }
            group.setMaxCapacity(updateGroupDto.getMaxCapacity());
            changed = true;
            log.debug("Updated max capacity for group {}", groupId);
        }

        if (changed) {
            CourseGroup updatedGroup = courseGroupRepository.save(group);
            log.info("Group {} updated successfully.", groupId);
            return mapToGroupResponse(updatedGroup);
        } else {
            log.info("No changes detected for group {}", groupId);
            return mapToGroupResponse(group);
        }
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        log.info("Attempting to delete group with ID: {}", groupId);
        if (!courseGroupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("CourseGroup", "id", groupId);
        }
        log.warn("Deleting group {}. This will trigger cascading deletes for Enrollments, Meetings, and Attendance based on entity mappings.", groupId);
        try {
            courseGroupRepository.deleteById(groupId);
            log.info("Group {} and potentially related entities deleted successfully via cascade.", groupId);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete group {} due to data integrity constraints. Check deeper relations or DB constraints. Original exception: {}", groupId, e.getMessage());
            throw new DeletionBlockedException("Cannot delete group: Deleting related data (enrollments, meetings, attendance) failed. " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsByCourseId(Long courseId) {
        log.debug("Fetching groups for course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        List<CourseGroup> groups = courseGroupRepository.findByCourseWithDetails(course);
        return groups.stream().map(this::mapToGroupResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllGroupsByCourseId(Long courseId) {
        log.info("Attempting to delete all groups for course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        List<CourseGroup> groupsToDelete = courseGroupRepository.findByCourse(course);

        if (!groupsToDelete.isEmpty()) {
            log.warn("Deleting {} groups for course {}. This will trigger cascading deletes for Enrollments, Meetings, etc. for each group.", groupsToDelete.size(), courseId);
            try {

                courseGroupRepository.deleteAll(groupsToDelete);
                log.info("Successfully deleted {} groups for course {}.", groupsToDelete.size(), courseId);
            } catch (DataIntegrityViolationException e) {
                log.error("Failed to delete groups for course {} due to data integrity constraints during cascade. Original exception: {}", courseId, e.getMessage());
                throw new DeletionBlockedException("Cannot delete all groups for course: Deleting related data failed for at least one group. " + e.getMessage());
            }
        } else {
            log.info("No groups found for course {}, nothing to delete.", courseId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAvailabilityResponse> findAvailableGroupsForStudent(Long courseId, String search, Pageable pageable) {
        log.debug("Finding available groups for current student. Filters: courseId={}, search='{}', pageable={}", courseId, search, pageable);
        Student currentStudent = studentService.findCurrentStudentEntity();

        List<Long> enrolledCourseIds = enrollmentRepository.findCourseIdsByStudent(currentStudent);

        Specification<CourseGroup> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("course", JoinType.INNER);
                Fetch<CourseGroup, Teacher> teacherFetch = root.fetch("teacher", JoinType.LEFT);
                teacherFetch.fetch("user", JoinType.LEFT);
            }
            query.distinct(true);


            List<Predicate> predicates = new ArrayList<>();

            if (courseId != null) {
                predicates.add(cb.equal(root.get("course").get("courseId"), courseId));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate courseCodeMatch = cb.like(cb.lower(root.get("course").get("courseCode")), pattern);
                Predicate courseNameMatch = cb.like(cb.lower(root.get("course").get("courseName")), pattern);
                Join<CourseGroup, Teacher> teacherJoin = root.join("teacher", JoinType.LEFT);
                Join<Teacher, User> userJoin = teacherJoin.join("user", JoinType.LEFT);
                Predicate teacherFirstNameMatch = cb.like(cb.lower(userJoin.get("firstName")), pattern);
                Predicate teacherLastNameMatch = cb.like(cb.lower(userJoin.get("lastName")), pattern);

                predicates.add(cb.or(courseCodeMatch, courseNameMatch, teacherFirstNameMatch, teacherLastNameMatch));
            }

            if (!enrolledCourseIds.isEmpty()) {
                predicates.add(root.get("course").get("courseId").in(enrolledCourseIds).not());
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };


        Page<CourseGroup> groupPage = courseGroupRepository.findAll(spec, pageable);

        List<GroupAvailabilityResponse> availabilityResponses = groupPage.getContent().stream()
                .map(this::mapToGroupAvailabilityResponse)
                .filter(g -> g.getAvailableSlots() > 0)
                .collect(Collectors.toList());

        log.info("Found {} available groups for student {}", availabilityResponses.size(), currentStudent.getStudentId());

        return new PageImpl<>(availabilityResponses, pageable, groupPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getEnrolledStudents(Long groupId, Pageable pageable) {
        log.debug("Fetching enrolled students for group ID: {}, pageable: {}", groupId, pageable);
        if (!courseGroupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("CourseGroup", "id", groupId);
        }

        Page<Student> studentPage = enrollmentRepository.findStudentsByGroupId(groupId, pageable);

        return studentPage.map(studentService::mapToStudentResponse);
    }


    private GroupResponse mapToGroupResponse(CourseGroup group) {
        if (group == null) return null;

        int enrolledCount = enrollmentRepository.countByGroup(group);

        return new GroupResponse(
                group.getCourseGroupId(),
                group.getGroupNumber(),
                group.getMaxCapacity(),
                mapToCourseMinimalResponse(group.getCourse()),
                mapToTeacherMinimalResponse(group.getTeacher()),
                enrolledCount
        );
    }

    private GroupAvailabilityResponse mapToGroupAvailabilityResponse(CourseGroup group) {
        if (group == null) return null;

        int enrolledCount = enrollmentRepository.countByGroup(group);
        int availableSlots = Math.max(0, group.getMaxCapacity() - enrolledCount);

        return new GroupAvailabilityResponse(
                group.getCourseGroupId(),
                group.getGroupNumber(),
                group.getMaxCapacity(),
                enrolledCount,
                availableSlots,
                mapToCourseResponse(group.getCourse()),
                mapToTeacherMinimalResponse(group.getTeacher())
        );
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return null;
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

    private CourseGroup findGroupByIdWithDetails(Long groupId) {
        return courseGroupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseGroup", "id", groupId));
    }

}