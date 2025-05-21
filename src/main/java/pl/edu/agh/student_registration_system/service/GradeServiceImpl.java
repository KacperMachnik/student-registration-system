package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.InvalidOperationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateGradeDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGradeDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.CourseRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.repository.GradeRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TeacherService teacherService;
    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    @Transactional
    public GradeResponse addGrade(CreateGradeDTO createGradeDto) {
        log.info("Attempting to add grade for student ID {} in course ID {}", createGradeDto.getStudentId(), createGradeDto.getCourseId());
        Student student = studentRepository.findById(createGradeDto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", createGradeDto.getStudentId()));
        Course course = courseRepository.findById(createGradeDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", createGradeDto.getCourseId()));
        Teacher currentTeacher = teacherService.findCurrentTeacherEntity();

        boolean isEnrolledInCourseByThisTeacher = enrollmentRepository.findByStudent(student).stream()
                .anyMatch(enrollment -> enrollment.getGroup().getCourse().equals(course) &&
                        enrollment.getGroup().getTeacher() != null &&
                        enrollment.getGroup().getTeacher().equals(currentTeacher));

        if (!isEnrolledInCourseByThisTeacher) {
            boolean teachesCourse = currentTeacher.getTaughtGroups().stream()
                    .anyMatch(group -> group.getCourse().equals(course));
            if (!teachesCourse) {
                log.warn("Teacher {} is not authorized to add grade for student {} in course {}. Teacher does not teach this course.",
                        currentTeacher.getTeacherId(), student.getStudentId(), course.getCourseId());
                throw new InvalidOperationException("Teacher is not authorized to add grade for this student in this course.");
            }
            log.warn("Teacher {} is adding grade for student {} in course {} although student may not be in their specific group, but teacher teaches the course.",
                    currentTeacher.getTeacherId(), student.getStudentId(), course.getCourseId());
        }


        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setTeacher(currentTeacher);
        grade.setGradeValue(createGradeDto.getGradeValue());
        grade.setComment(createGradeDto.getComment());
        grade.setGradeDate(LocalDateTime.now());

        Grade savedGrade = gradeRepository.save(grade);
        log.info("Grade (ID: {}) added successfully by teacher ID {}", savedGrade.getGradeId(), currentTeacher.getTeacherId());
        return mapToGradeResponse(savedGrade);
    }

    @Override
    @Transactional
    public GradeResponse updateGrade(Long gradeId, UpdateGradeDTO updateGradeDto) {
        log.info("Attempting to update grade ID {}", gradeId);
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", gradeId));


        grade.setGradeValue(updateGradeDto.getGradeValue());
        grade.setComment(updateGradeDto.getComment());

        Grade updatedGrade = gradeRepository.save(grade);
        log.info("Grade (ID: {}) updated successfully.", updatedGrade.getGradeId());
        return mapToGradeResponse(updatedGrade);
    }

    @Override
    @Transactional
    public void deleteGrade(Long gradeId) {
        log.info("Attempting to delete grade ID {}", gradeId);
        if (!gradeRepository.existsById(gradeId)) {
            throw new ResourceNotFoundException("Grade", "id", gradeId);
        }
        gradeRepository.deleteById(gradeId);
        log.info("Grade (ID: {}) deleted successfully.", gradeId);
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
        if (student == null || student.getUser() == null) return null;
        User user = student.getUser();
        return new StudentMinimalResponse(
                student.getStudentId(),
                user.getFirstName(),
                user.getLastName(),
                student.getIndexNumber()
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
        if (teacher == null || teacher.getUser() == null) return null;
        User user = teacher.getUser();
        return new TeacherMinimalResponse(
                teacher.getTeacherId(),
                user.getFirstName(),
                user.getLastName(),
                teacher.getTitle()
        );
    }
}