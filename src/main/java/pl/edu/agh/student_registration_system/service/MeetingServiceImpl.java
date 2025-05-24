package pl.edu.agh.student_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.student_registration_system.exceptions.InvalidOperationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.payload.dto.DefineMeetingDTO;
import pl.edu.agh.student_registration_system.payload.response.MeetingResponse;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final CourseGroupRepository courseGroupRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    @Transactional
    public List<MeetingResponse> defineMeetingsForGroup(Long groupId, DefineMeetingDTO defineMeetingDto) {
        log.info("Defining {} meetings for group ID: {} starting from {}",
                defineMeetingDto.getNumberOfMeetings(), groupId, defineMeetingDto.getFirstMeetingDateTime());

        CourseGroup group = courseGroupRepository.findByIdWithDetails(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseGroup", "id", groupId));

        if (group.getCourse() == null) {
            log.error("CourseGroup with ID {} does not have an associated Course. Cannot generate default topics.", groupId);
            throw new IllegalStateException("CourseGroup is missing its associated Course.");
        }

        List<String> providedTopics = defineMeetingDto.getTopics();
        if (providedTopics != null && !providedTopics.isEmpty() && providedTopics.size() != defineMeetingDto.getNumberOfMeetings()) {
            log.warn("Number of provided topics ({}) does not match the number of meetings to be created ({}).",
                    providedTopics.size(), defineMeetingDto.getNumberOfMeetings());
            throw new InvalidOperationException(
                    "If 'topics' list is provided, it must contain exactly " +
                            defineMeetingDto.getNumberOfMeetings() + " elements, or be null/empty to use default topic generation."
            );
        }


        int lastMeetingNumber = meetingRepository.findTopByGroupOrderByMeetingNumberDesc(group)
                .map(Meeting::getMeetingNumber)
                .orElse(0);

        List<Meeting> meetingsToCreate = new ArrayList<>();
        LocalDateTime currentDateTime = defineMeetingDto.getFirstMeetingDateTime();
        String fallbackTopic = defineMeetingDto.getTopic();

        for (int i = 0; i < defineMeetingDto.getNumberOfMeetings(); i++) {
            Meeting meeting = new Meeting();
            meeting.setGroup(group);
            int currentMeetingNumber = lastMeetingNumber + i + 1;
            meeting.setMeetingNumber(currentMeetingNumber);
            meeting.setMeetingDate(currentDateTime);

            String meetingTopic;
            if (providedTopics != null && !providedTopics.isEmpty()) {
                meetingTopic = providedTopics.get(i);
                if (meetingTopic == null || meetingTopic.isBlank()) {
                    meetingTopic = group.getCourse().getCourseName() + " - Spotkanie " + currentMeetingNumber;
                }
            } else if (fallbackTopic != null && !fallbackTopic.isBlank()) {
                meetingTopic = fallbackTopic + " - Spotkanie " + currentMeetingNumber;
            } else {
                meetingTopic = group.getCourse().getCourseName() + " - Spotkanie " + currentMeetingNumber;
            }
            meeting.setTopic(meetingTopic);

            meetingsToCreate.add(meeting);
            currentDateTime = currentDateTime.plusWeeks(1);
        }

        List<Meeting> savedMeetings = meetingRepository.saveAll(meetingsToCreate);
        log.info("{} meetings defined and saved for group ID: {}", savedMeetings.size(), groupId);
        log.info("Random meeting time from db {}", savedMeetings.getFirst().getMeetingDate());
        return savedMeetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetingsByGroupId(Long groupId) {
        log.info("Fetching meetings for group ID: {}", groupId);
        if (!courseGroupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("CourseGroup", "id", groupId);
        }
        CourseGroup group = courseGroupRepository.getReferenceById(groupId);
        List<Meeting> meetings = meetingRepository.findByGroupOrderByMeetingNumber(group);
        return meetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        if (meeting == null) return null;
        return new MeetingResponse(
                meeting.getMeetingId(),
                meeting.getMeetingNumber(),
                meeting.getMeetingDate() != null ? meeting.getMeetingDate().format(DATE_FORMATTER) : null,
                meeting.getTopic(),
                meeting.getGroup() != null ? meeting.getGroup().getCourseGroupId() : null
        );
    }
}