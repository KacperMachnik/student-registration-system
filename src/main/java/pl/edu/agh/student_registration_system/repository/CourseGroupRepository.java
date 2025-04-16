package pl.edu.agh.student_registration_system.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Student;
import pl.edu.agh.student_registration_system.model.Teacher;

import java.util.List;
import java.util.Optional;

public interface CourseGroupRepository extends JpaRepository<CourseGroup, Long>, JpaSpecificationExecutor<CourseGroup> {

    @Query("SELECT CASE WHEN COUNT(cg) > 0 THEN true ELSE false END " +
            "FROM course_groups cg JOIN cg.enrollments e " +
            "WHERE cg.teacher = :teacher AND e.student = :student")
    boolean existsByTeacherAndEnrollmentsStudent(@Param("teacher") Teacher teacher, @Param("student") Student student);

    Long countByTeacher(Teacher teacher);

    @Query("SELECT cg FROM course_groups cg JOIN FETCH cg.course c WHERE cg.teacher = :teacher")
    List<CourseGroup> findByTeacherWithCourse(@Param("teacher") Teacher teacher);

    @Query("SELECT cg FROM course_groups cg " +
            "JOIN FETCH cg.course c " +
            "JOIN FETCH cg.teacher t " +
            "JOIN FETCH t.user tu " +
            "WHERE cg.teacher = :teacher AND cg.course = :course")
    List<CourseGroup> findByTeacherAndCourseWithDetails(@Param("teacher") Teacher teacher, @Param("course") Course course);

    @Query("SELECT cg FROM course_groups cg " +
            "JOIN FETCH cg.course c " +
            "JOIN FETCH cg.teacher t " +
            "JOIN FETCH t.user tu " +
            "WHERE cg.teacher = :teacher")
    List<CourseGroup> findByTeacherWithDetails(@Param("teacher") Teacher teacher);

    @Query("SELECT cg FROM course_groups cg " +
            "LEFT JOIN FETCH cg.teacher t " +
            "LEFT JOIN FETCH t.user tu " +
            "JOIN FETCH cg.course c " +
            "WHERE c = :course")
    List<CourseGroup> findByCourseWithDetails(@Param("course") Course course);


    @Query("SELECT cg FROM course_groups cg " +
            "JOIN FETCH cg.course c " +
            "LEFT JOIN FETCH cg.teacher t " +
            "LEFT JOIN FETCH t.user tu " +
            "WHERE cg.courseGroupId = :groupId")
    Optional<CourseGroup> findByIdWithDetails(@Param("groupId") Long groupId);

    List<CourseGroup> findByCourse(Course course);

    boolean existsByCourseAndGroupNumberAndCourseGroupIdNot(Course course, Integer groupNumber, Long groupId);

    boolean existsByCourseAndGroupNumber(Course course, @NotNull Integer groupNumber);
}