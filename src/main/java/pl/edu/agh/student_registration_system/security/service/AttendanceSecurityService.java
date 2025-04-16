package pl.edu.agh.student_registration_system.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.model.Attendance;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.AttendanceRepository;
import pl.edu.agh.student_registration_system.service.TeacherService;

@Service("attendanceSecurityService")
@RequiredArgsConstructor
public class AttendanceSecurityService {

    private final TeacherService teacherService;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public boolean canTeacherUpdateAttendance(Long attendanceId) {
        Teacher currentTeacher = teacherService.findCurrentTeacherEntity();
        Attendance attendance = attendanceRepository.findById(attendanceId).orElse(null);
        return attendance != null && attendance.getRecordedByTeacher() != null
                && attendance.getRecordedByTeacher().getTeacherId().equals(currentTeacher.getTeacherId());
    }
}
