package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.Grade;
import pl.edu.agh.student_registration_system.model.Student;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g FROM grades g " +
            "JOIN FETCH g.student s " +
            "JOIN FETCH s.user su " +
            "JOIN FETCH g.course c " +
            "JOIN FETCH g.teacher t " +
            "JOIN FETCH t.user tu " +
            "WHERE g.student = :student AND g.course = :course")
    List<Grade> findByStudentAndCourseWithDetails(@Param("student") Student student, @Param("course") Course course);

    @Query("SELECT g FROM grades g " +
            "JOIN FETCH g.student s " +
            "JOIN FETCH s.user su " +
            "JOIN FETCH g.course c " +
            "JOIN FETCH g.teacher t " +
            "JOIN FETCH t.user tu " +
            "WHERE g.student = :student")
    List<Grade> findByStudentWithDetails(@Param("student") Student student);

    @Query("SELECT g FROM grades g " +
            "JOIN FETCH g.student s " +
            "JOIN FETCH s.user su " +
            "JOIN FETCH g.course c " +
            "JOIN FETCH g.teacher t " +
            "JOIN FETCH t.user tu " +
            "WHERE g.student = :student AND g.course = :course")
    List<Grade> findAllByStudentAndCourseWithDetails(@Param("student") Student student, @Param("course") Course course);

}
