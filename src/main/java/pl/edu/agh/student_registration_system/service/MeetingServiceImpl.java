package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.payload.dto.DefineMeetingDTO;
import pl.edu.agh.student_registration_system.payload.response.MeetingResponse;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final CourseGroupRepository courseGroupRepository;

    @Override
    @Transactional
    public List<MeetingResponse> defineMeetingsForGroup(Long groupId, DefineMeetingDTO defineMeetingDto) {
        log.info("Defining {} meetings for group ID: {}", defineMeetingDto.getNumberOfMeetings(), groupId);
        CourseGroup group = courseGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseGroup", "id", groupId));

        int lastMeetingNumber = meetingRepository.findTopByGroupOrderByMeetingNumberDesc(group)
                .map(Meeting::getMeetingNumber)
                .orElse(0);

        List<Meeting> meetingsToCreate = new ArrayList<>();
        LocalDateTime currentDateTime = defineMeetingDto.getFirstMeetingDateTime();

        for (int i = 1; i <= defineMeetingDto.getNumberOfMeetings(); i++) {
            Meeting meeting = new Meeting();
            meeting.setGroup(group);
            meeting.setMeetingNumber(lastMeetingNumber + i);
            meeting.setMeetingDate(currentDateTime);
            meeting.setTopic(defineMeetingDto.getTopic());
            meetingsToCreate.add(meeting);
            currentDateTime = currentDateTime.plusWeeks(1);
        }

        List<Meeting> savedMeetings = meetingRepository.saveAll(meetingsToCreate);
        log.info("{} meetings defined and saved for group ID: {}", savedMeetings.size(), groupId);
        return savedMeetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetingsByGroupId(Long groupId) {
        log.info("Fetching meetings for group ID: {}", groupId);
        CourseGroup group = courseGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseGroup", "id", groupId));
        List<Meeting> meetings = meetingRepository.findByGroupOrderByMeetingNumber(group);
        return meetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        return new MeetingResponse(
                meeting.getMeetingId(),
                meeting.getMeetingNumber(),
                meeting.getMeetingDate() != null ? meeting.getMeetingDate().toLocalDate().toString() : null,
                meeting.getTopic(),
                meeting.getGroup().getCourseGroupId()
        );
    }
}
