package pl.edu.agh.student_registration_system.service;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.exceptions.UserAlreadyExistsException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.UpdateStudentDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final UserService userService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentResponseById(Long studentId) {
        log.debug("Fetching student response by ID: {}", studentId);
        Student student = findStudentById(studentId);
        return mapToStudentResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getCurrentStudentResponse() {
        log.debug("Fetching current student response");
        Student student = findCurrentStudentEntity();
        return mapToStudentResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long studentId, UpdateStudentDTO updateStudentDto) {
        log.info("Attempting to update student with ID: {}", studentId);
        Student student = findStudentById(studentId);
        User user = student.getUser();

        boolean changed = false;

        if (updateStudentDto.getEmail() != null && !updateStudentDto.getEmail().isBlank() && !updateStudentDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateStudentDto.getEmail())) {
                log.warn("Update failed for student {}: Email {} is already in use.", studentId, updateStudentDto.getEmail());
                throw new UserAlreadyExistsException("Email '" + updateStudentDto.getEmail() + "' is already in use!");
            }
            log.debug("Updating email for user {} (student {})", user.getUserId(), studentId);
            user.setEmail(updateStudentDto.getEmail());
            changed = true;
        }
        if (updateStudentDto.getFirstName() != null && !updateStudentDto.getFirstName().isBlank() && !updateStudentDto.getFirstName().equals(user.getFirstName())) {
            log.debug("Updating first name for user {} (student {})", user.getUserId(), studentId);
            user.setFirstName(updateStudentDto.getFirstName());
            changed = true;
        }
        if (updateStudentDto.getLastName() != null && !updateStudentDto.getLastName().isBlank() && !updateStudentDto.getLastName().equals(user.getLastName())) {
            log.debug("Updating last name for user {} (student {})", user.getUserId(), studentId);
            user.setLastName(updateStudentDto.getLastName());
            changed = true;
        }
        if (updateStudentDto.getIsActive() != null && !updateStudentDto.getIsActive().equals(user.getIsActive())) {
            log.debug("Updating isActive status for user {} (student {}) to {}", user.getUserId(), studentId, updateStudentDto.getIsActive());
            user.setIsActive(updateStudentDto.getIsActive());
            changed = true;
        }


        if (changed) {
            User updatedUser = userRepository.save(user);
            log.info("User {} (student {}) updated successfully.", updatedUser.getUserId(), studentId);
        } else {
            log.info("No changes detected for student with ID: {}", studentId);
        }

        return mapToStudentResponse(student);
    }

    @Override
    @Transactional
    public void deleteStudentAndUser(Long studentId) {
        log.info("Attempting to delete student and associated user for student ID: {}", studentId);
        Student student = findStudentById(studentId);
        User userToDelete = student.getUser();

        log.warn("Deleting user {} which will cascade delete student profile {}", userToDelete.getUserId(), studentId);
        userRepository.delete(userToDelete);
        log.info("User {} and associated student profile {} deleted successfully.", userToDelete.getUserId(), studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> searchStudents(String search, Pageable pageable) {
        log.debug("Searching for students with query: '{}', pageable: {}", search, pageable);

        Specification<Student> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.INNER).fetch("role", JoinType.INNER);
            }
            query.distinct(true);

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";

            Join<Student, User> userJoin = root.join("user");
            Predicate firstNameMatch = cb.like(cb.lower(userJoin.get("firstName")), pattern);
            Predicate lastNameMatch = cb.like(cb.lower(userJoin.get("lastName")), pattern);
            Predicate emailMatch = cb.like(cb.lower(userJoin.get("email")), pattern);
            Predicate indexNumberMatch = cb.like(cb.lower(root.get("indexNumber")), pattern);

            return cb.or(firstNameMatch, lastNameMatch, emailMatch, indexNumberMatch);
        };

        Page<Student> studentPage = studentRepository.findAll(spec, pageable);
        log.debug("Found {} students matching query '{}'", studentPage.getTotalElements(), search);

        return studentPage.map(this::mapToStudentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getCurrentStudentGroups() {
        log.debug("Fetching groups for the current student");
        Student currentStudent = findCurrentStudentEntity();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentWithGroupAndCourse(currentStudent);

        if (enrollments.isEmpty()) {
            log.debug("Current student (ID: {}) is not enrolled in any groups.", currentStudent.getStudentId());
            return Collections.emptyList();
        }
        log.debug("Found {} enrollments for student {}", enrollments.size(), currentStudent.getStudentId());

        return enrollments.stream()
                .map(Enrollment::getGroup)
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getCurrentStudentGrades(Long courseId) {
        log.debug("Fetching grades for the current student. Course filter ID: {}", courseId);
        Student currentStudent = findCurrentStudentEntity();

        List<Grade> grades;
        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
            grades = gradeRepository.findByStudentAndCourseWithDetails(currentStudent, course);
            log.debug("Found {} grades for student {} and course {}", grades.size(), currentStudent.getStudentId(), courseId);
        } else {
            grades = gradeRepository.findByStudentWithDetails(currentStudent);
            log.debug("Found {} grades in total for student {}", grades.size(), currentStudent.getStudentId());
        }

        return grades.stream()
                .map(this::mapToGradeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getCurrentStudentAttendance(Long groupId, Long courseId, Integer meetingNumber) {
        log.debug("Fetching attendance for current student. Filters: groupId={}, courseId={}, meetingNumber={}",
                groupId, courseId, meetingNumber);
        Student currentStudent = findCurrentStudentEntity();

        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("student"), currentStudent));

            Join<Attendance, Meeting> meetingJoin = root.join("meeting");

            if (groupId != null) {
                predicates.add(cb.equal(meetingJoin.get("group").get("courseGroupId"), groupId));
            }
            if (courseId != null) {

                Join<Meeting, CourseGroup> groupJoin = meetingJoin.join("group");
                predicates.add(cb.equal(groupJoin.get("course").get("courseId"), courseId));
            }
            if (meetingNumber != null) {
                predicates.add(cb.equal(meetingJoin.get("meetingNumber"), meetingNumber));
            }


            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("student", JoinType.INNER).fetch("user", JoinType.INNER);
                Fetch<Attendance, Meeting> meetingFetch = root.fetch("meeting", JoinType.INNER);
                Fetch<Meeting, CourseGroup> groupFetch = meetingFetch.fetch("group", JoinType.INNER);
                groupFetch.fetch("course", JoinType.INNER);
                Fetch<CourseGroup, Teacher> teacherFetch = groupFetch.fetch("teacher", JoinType.LEFT);
                if (teacherFetch != null) {
                    teacherFetch.fetch("user", JoinType.LEFT);
                }
                root.fetch("recordedByTeacher", JoinType.INNER).fetch("user", JoinType.INNER);
            }
            query.orderBy(cb.asc(meetingJoin.get("meetingDate")), cb.asc(meetingJoin.get("meetingNumber")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Attendance> attendanceList = attendanceRepository.findAll(spec);
        log.debug("Found {} attendance records for student {}", attendanceList.size(), currentStudent.getStudentId());
        return attendanceList.stream().map(this::mapToAttendanceResponse).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public Student findStudentById(Long studentId) {
        return studentRepository.findByIdWithUser(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found with ID: {}", studentId);
                    return new ResourceNotFoundException("Student", "id", studentId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Student findCurrentStudentEntity() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return studentRepository.findByUser(currentUser)
                .orElseThrow(() -> {
                    log.warn("Student profile not found for current user {}", currentUser.getEmail());
                    return new ResourceNotFoundException("Student profile", "currentUser", currentUser.getEmail());
                });
    }


    @Override
    public StudentResponse mapToStudentResponse(Student student) {
        if (student == null) return null;

        User user = student.getUser();
        if (user == null) {
            log.error("Student (ID: {}) is missing associated User data.", student.getStudentId());
            throw new IllegalStateException("Student (ID: " + student.getStudentId() + ") is missing associated User data.");
        }
        if (user.getRole() == null) {
            log.error("User (ID: {}) associated with Student (ID: {}) is missing Role data.", user.getUserId(), student.getStudentId());
            throw new IllegalStateException("User (ID: " + user.getUserId() + ") is missing Role data.");
        }


        Long userId = user.getUserId();
        String usernameOrEmail = user.getEmail();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        Boolean isActive = user.getIsActive();

        List<String> roles = List.of(user.getRole().getRoleName().name());

        LoginResponse userInfo = new LoginResponse(userId, usernameOrEmail, firstName, lastName, isActive, roles);

        return new StudentResponse(student.getStudentId(), student.getIndexNumber(), userInfo);
    }


    private GroupResponse mapToGroupResponse(CourseGroup group) {
        if (group == null) return null;
        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getCourseGroupId());
        response.setGroupNumber(group.getGroupNumber());
        response.setMaxCapacity(group.getMaxCapacity());
        response.setCourse(mapToCourseMinimalResponse(group.getCourse()));
        response.setTeacher(mapToTeacherMinimalResponse(group.getTeacher()));
        response.setEnrolledCount(enrollmentRepository.countByGroup(group));

        return response;
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
        User teacherUser = teacher.getUser();
        return new TeacherMinimalResponse(
                teacher.getTeacherId(),
                teacherUser != null ? teacherUser.getFirstName() : null,
                teacherUser != null ? teacherUser.getLastName() : null,
                teacher.getTitle()
        );
    }

    private GradeResponse mapToGradeResponse(Grade grade) {
        if (grade == null) return null;

        return new GradeResponse(
                grade.getGradeId(),
                grade.getGradeValue(),
                grade.getGradeDate() != null ? grade.getGradeDate().format(ISO_DATE_TIME_FORMATTER) : null,
                grade.getComment(),
                mapToStudentMinimalResponse(grade.getStudent()),
                mapToCourseMinimalResponse(grade.getCourse()),
                mapToTeacherMinimalResponse(grade.getTeacher())
        );
    }

    private StudentMinimalResponse mapToStudentMinimalResponse(Student student) {
        if (student == null) return null;
        User user = student.getUser();
        return new StudentMinimalResponse(
                student.getStudentId(),
                user != null ? user.getFirstName() : null,
                user != null ? user.getLastName() : null,
                student.getIndexNumber()
        );
    }


    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        if (attendance == null) return null;

        Meeting meeting = attendance.getMeeting();
        Student student = attendance.getStudent();
        Teacher recorder = attendance.getRecordedByTeacher();

        MeetingMinimalResponse meetingInfo = meeting != null ?
                new MeetingMinimalResponse(
                        meeting.getMeetingId(),
                        meeting.getMeetingNumber(),
                        meeting.getMeetingDate() != null ? meeting.getMeetingDate().format(ISO_DATE_TIME_FORMATTER) : null)
                : null;

        StudentMinimalResponse studentInfo = mapToStudentMinimalResponse(student);
        TeacherMinimalResponse recorderInfo = mapToTeacherMinimalResponse(recorder);


        return new AttendanceResponse(
                attendance.getAttendanceId(),
                attendance.getStatus() != null ? attendance.getStatus().name() : null,
                meetingInfo,
                studentInfo,
                recorderInfo
        );
    }
}