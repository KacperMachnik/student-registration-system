package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.payload.dto.DefineMeetingDTO;
import pl.edu.agh.student_registration_system.payload.response.MeetingResponse;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private CourseGroupRepository courseGroupRepository;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    private CourseGroup testGroup;
    private Meeting testMeeting;
    private DefineMeetingDTO defineMeetingDTO;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        Course testCourse = new Course();
        testCourse.setCourseId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");

        testGroup = new CourseGroup();
        testGroup.setCourseGroupId(1L);
        testGroup.setGroupNumber(1);
        testGroup.setMaxCapacity(30);
        testGroup.setCourse(testCourse);

        testDateTime = LocalDateTime.of(2025, 5, 1, 10, 0);

        testMeeting = new Meeting();
        testMeeting.setMeetingId(1L);
        testMeeting.setMeetingNumber(1);
        testMeeting.setMeetingDate(testDateTime);
        testMeeting.setTopic("Introduction");
        testMeeting.setGroup(testGroup);

        defineMeetingDTO = new DefineMeetingDTO();
        defineMeetingDTO.setNumberOfMeetings(3);
        defineMeetingDTO.setFirstMeetingDateTime(testDateTime);
        defineMeetingDTO.setTopic("Weekly Lecture");
    }

    @Test
    void defineMeetingsForGroup_ShouldReturnListOfMeetingResponses_WhenGroupExists() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(testGroup)).thenReturn(Optional.empty());

        List<Meeting> savedMeetings = List.of(
                createMeeting(1L, 1, testDateTime, "Weekly Lecture", testGroup),
                createMeeting(2L, 2, testDateTime.plusWeeks(1), "Weekly Lecture", testGroup),
                createMeeting(3L, 3, testDateTime.plusWeeks(2), "Weekly Lecture", testGroup)
        );

        when(meetingRepository.saveAll(anyList())).thenReturn(savedMeetings);

        List<MeetingResponse> result = meetingService.defineMeetingsForGroup(1L, defineMeetingDTO);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getMeetingId());
        assertEquals(1, result.get(0).getMeetingNumber());
        assertEquals(testDateTime.toLocalDate().toString(), result.get(0).getMeetingDate());
        assertEquals("Weekly Lecture", result.get(0).getTopic());
        assertEquals(1L, result.get(0).getGroupId());

        verify(courseGroupRepository).findById(1L);
        verify(meetingRepository).findTopByGroupOrderByMeetingNumberDesc(testGroup);
        verify(meetingRepository).saveAll(anyList());
    }

    @Test
    void defineMeetingsForGroup_ShouldContinueNumberingFromLastMeeting_WhenMeetingsExist() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(testGroup)).thenReturn(Optional.of(testMeeting));

        List<Meeting> savedMeetings = List.of(
                createMeeting(2L, 2, testDateTime, "Weekly Lecture", testGroup),
                createMeeting(3L, 3, testDateTime.plusWeeks(1), "Weekly Lecture", testGroup),
                createMeeting(4L, 4, testDateTime.plusWeeks(2), "Weekly Lecture", testGroup)
        );

        when(meetingRepository.saveAll(anyList())).thenReturn(savedMeetings);

        List<MeetingResponse> result = meetingService.defineMeetingsForGroup(1L, defineMeetingDTO);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(2, result.get(0).getMeetingNumber());
        assertEquals(3, result.get(1).getMeetingNumber());
        assertEquals(4, result.get(2).getMeetingNumber());

        verify(courseGroupRepository).findById(1L);
        verify(meetingRepository).findTopByGroupOrderByMeetingNumberDesc(testGroup);
        verify(meetingRepository).saveAll(anyList());
    }

    @Test
    void defineMeetingsForGroup_ShouldThrowResourceNotFoundException_WhenGroupDoesNotExist() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> meetingService.defineMeetingsForGroup(1L, defineMeetingDTO));

        verify(courseGroupRepository).findById(1L);
        verify(meetingRepository, never()).findTopByGroupOrderByMeetingNumberDesc(any());
        verify(meetingRepository, never()).saveAll(anyList());
    }

    @Test
    void getMeetingsByGroupId_ShouldReturnListOfMeetingResponses_WhenGroupExists() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(meetingRepository.findByGroupOrderByMeetingNumber(testGroup)).thenReturn(List.of(testMeeting));

        List<MeetingResponse> result = meetingService.getMeetingsByGroupId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getMeetingId());
        assertEquals(1, result.get(0).getMeetingNumber());
        assertEquals(testDateTime.toLocalDate().toString(), result.get(0).getMeetingDate());
        assertEquals("Introduction", result.get(0).getTopic());
        assertEquals(1L, result.get(0).getGroupId());

        verify(courseGroupRepository).findById(1L);
        verify(meetingRepository).findByGroupOrderByMeetingNumber(testGroup);
    }

    @Test
    void getMeetingsByGroupId_ShouldThrowResourceNotFoundException_WhenGroupDoesNotExist() {
        when(courseGroupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingsByGroupId(1L));

        verify(courseGroupRepository).findById(1L);
        verify(meetingRepository, never()).findByGroupOrderByMeetingNumber(any());
    }

    private Meeting createMeeting(Long id, Integer number, LocalDateTime date, String topic, CourseGroup group) {
        Meeting meeting = new Meeting();
        meeting.setMeetingId(id);
        meeting.setMeetingNumber(number);
        meeting.setMeetingDate(date);
        meeting.setTopic(topic);
        meeting.setGroup(group);
        return meeting;
    }
}
