package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.InvalidOperationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.*;
import pl.edu.agh.student_registration_system.payload.dto.AttendanceRecordDTO;
import pl.edu.agh.student_registration_system.payload.dto.RecordAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateAttendanceDTO;
import pl.edu.agh.student_registration_system.payload.response.*;
import pl.edu.agh.student_registration_system.repository.AttendanceRepository;
import pl.edu.agh.student_registration_system.repository.EnrollmentRepository;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;
import pl.edu.agh.student_registration_system.repository.StudentRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final StudentRepository studentRepository;
    private final TeacherService teacherService;
    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    @Transactional
    public List<AttendanceResponse> recordAttendanceForMeeting(Long meetingId, RecordAttendanceDTO recordAttendanceDto) {
        log.info("Recording attendance for meeting ID: {}", meetingId);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", meetingId));
        Teacher currentTeacher = teacherService.findCurrentTeacherEntity();
        CourseGroup group = meeting.getGroup();

        List<Attendance> attendanceListToSave = new ArrayList<>();

        for (AttendanceRecordDTO record : recordAttendanceDto.getAttendanceList()) {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", record.getStudentId()));

            boolean isEnrolled = enrollmentRepository.existsByStudentAndGroup_CourseGroupId(student, group.getCourseGroupId());
            if (!isEnrolled) {
                log.warn("Student {} is not enrolled in group {}. Skipping attendance record.", student.getStudentId(), group.getCourseGroupId());
                throw new InvalidOperationException("Student " + student.getStudentId() + " is not enrolled in group " + group.getCourseGroupId());
            }

            Optional<Attendance> existingAttendanceOpt = attendanceRepository.findByMeetingAndStudent(meeting, student);
            Attendance attendance;
            if (existingAttendanceOpt.isPresent()) {
                attendance = existingAttendanceOpt.get();
                log.debug("Updating existing attendance for student {} in meeting {}", student.getStudentId(), meetingId);
            } else {
                attendance = new Attendance();
                attendance.setMeeting(meeting);
                attendance.setStudent(student);
                log.debug("Creating new attendance for student {} in meeting {}", student.getStudentId(), meetingId);
            }

            try {
                attendance.setStatus(AttendanceStatus.valueOf(record.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid attendance status: {}", record.getStatus());
                throw new InvalidOperationException("Invalid attendance status: " + record.getStatus());
            }
            attendance.setRecordedByTeacher(currentTeacher);
            attendanceListToSave.add(attendance);
        }

        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendanceListToSave);
        log.info("Successfully recorded/updated {} attendance records for meeting ID: {}", savedAttendances.size(), meetingId);
        return savedAttendances.stream().map(this::mapToAttendanceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByMeetingId(Long meetingId) {
        log.debug("Fetching attendance for meeting ID: {}", meetingId);
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Meeting", "id", meetingId);
        }
        List<Attendance> attendances = attendanceRepository.findAll((root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("student").fetch("user");
                root.fetch("meeting");
                root.fetch("recordedByTeacher").fetch("user");
            }
            return cb.equal(root.get("meeting").get("meetingId"), meetingId);
        });
        return attendances.stream().map(this::mapToAttendanceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceResponse updateSingleAttendance(Long attendanceId, UpdateAttendanceDTO updateAttendanceDto) {
        log.info("Updating single attendance record ID: {}", attendanceId);
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));


        try {
            attendance.setStatus(AttendanceStatus.valueOf(updateAttendanceDto.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid attendance status: {}", updateAttendanceDto.getStatus());
            throw new InvalidOperationException("Invalid attendance status: " + updateAttendanceDto.getStatus());
        }

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance record ID {} updated successfully.", attendanceId);
        return mapToAttendanceResponse(updatedAttendance);
    }

    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        if (attendance == null) return null;

        return new AttendanceResponse(
                attendance.getAttendanceId(),
                attendance.getStatus() != null ? attendance.getStatus().name() : null,
                mapToMeetingMinimalResponse(attendance.getMeeting()),
                mapToStudentMinimalResponse(attendance.getStudent()),
                mapToTeacherMinimalResponse(attendance.getRecordedByTeacher())
        );
    }

    private MeetingMinimalResponse mapToMeetingMinimalResponse(Meeting meeting) {
        if (meeting == null) return null;
        return new MeetingMinimalResponse(
                meeting.getMeetingId(),
                meeting.getMeetingNumber(),
                meeting.getMeetingDate() != null ? meeting.getMeetingDate().format(ISO_DATE_TIME_FORMATTER) : null
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