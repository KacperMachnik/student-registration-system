package pl.edu.agh.student_registration_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Enrollment;
import pl.edu.agh.student_registration_system.model.Student;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(Student student);

    Integer countByGroup(CourseGroup group);

    @Query("SELECT e FROM enrollments e " +
            "JOIN FETCH e.group g " +
            "JOIN FETCH g.course c " +
            "LEFT JOIN FETCH g.teacher t " +
            "LEFT JOIN FETCH t.user tu " +
            "WHERE e.student = :student")
    List<Enrollment> findByStudentWithGroupAndCourse(@Param("student") Student student);

    @Query(value = "SELECT s FROM enrollments e JOIN e.student s " +
            "JOIN FETCH s.user u " +
            "JOIN FETCH u.role " +
            "WHERE e.group.courseGroupId = :groupId",
            countQuery = "SELECT count(e.student) FROM enrollments e WHERE e.group.courseGroupId = :groupId")
    Page<Student> findStudentsByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query("SELECT DISTINCT e.group.course.courseId FROM enrollments e WHERE e.student = :student")
    List<Long> findCourseIdsByStudent(@Param("student") Student student);

    boolean existsByStudentAndGroup_Course(Student student, Course course);

    Optional<Enrollment> findByStudentAndGroup_CourseGroupId(Student student, Long groupId);

    List<Enrollment> findByGroup(CourseGroup group);

    boolean existsByStudentAndGroup_CourseGroupId(Student currentStudent, Long groupId);
}