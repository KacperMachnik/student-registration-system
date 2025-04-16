package pl.edu.agh.student_registration_system.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.model.Teacher;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;
import pl.edu.agh.student_registration_system.service.TeacherService;

@Service("meetingSecurityService")
@RequiredArgsConstructor
public class MeetingSecurityService {

    private final TeacherService teacherService;
    private final MeetingRepository meetingRepository;

    @Transactional(readOnly = true)
    public boolean isTeacherForMeeting(Long meetingId) {
        Teacher currentTeacher = teacherService.findCurrentTeacherEntity();
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        return meeting != null && meeting.getGroup() != null && meeting.getGroup().getTeacher() != null
                && meeting.getGroup().getTeacher().getTeacherId().equals(currentTeacher.getTeacherId());
    }
}