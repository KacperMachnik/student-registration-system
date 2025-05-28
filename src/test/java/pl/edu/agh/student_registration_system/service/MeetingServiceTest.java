package pl.edu.agh.student_registration_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.student_registration_system.exceptions.InvalidOperationException;
import pl.edu.agh.student_registration_system.exceptions.ResourceNotFoundException;
import pl.edu.agh.student_registration_system.model.Course;
import pl.edu.agh.student_registration_system.model.CourseGroup;
import pl.edu.agh.student_registration_system.model.Meeting;
import pl.edu.agh.student_registration_system.payload.dto.DefineMeetingDTO;
import pl.edu.agh.student_registration_system.payload.response.MeetingResponse;
import pl.edu.agh.student_registration_system.repository.CourseGroupRepository;
import pl.edu.agh.student_registration_system.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
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

    private CourseGroup courseGroup;
    private Course course;
    private DefineMeetingDTO defineMeetingDTOWithoutTopicsList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @BeforeEach
    void setUp() {
        course = new Course(1L, "Test Course", "TC101", "Desc", 3, null, null);
        courseGroup = new CourseGroup(1L, 1, 30, course, null, null, null);
        defineMeetingDTOWithoutTopicsList = new DefineMeetingDTO(2, LocalDateTime.of(2024, 1, 1, 10, 0), null, "Default Topic");
    }

    @Test
    void defineMeetingsForGroup_Success_DefaultTopicsFromFallbackString() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(courseGroup)).thenReturn(Optional.empty());
        when(meetingRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Meeting> meetings = invocation.getArgument(0);
            for (int i = 0; i < meetings.size(); i++) {
                meetings.get(i).setMeetingId((long) (i + 1));
            }
            return meetings;
        });

        List<MeetingResponse> responses = meetingService.defineMeetingsForGroup(1L, defineMeetingDTOWithoutTopicsList);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Default Topic - Spotkanie 1", responses.get(0).getTopic());
        assertEquals("Default Topic - Spotkanie 2", responses.get(1).getTopic());
        assertEquals(defineMeetingDTOWithoutTopicsList.getFirstMeetingDateTime().format(DATE_FORMATTER), responses.get(0).getMeetingDate());
        assertEquals(defineMeetingDTOWithoutTopicsList.getFirstMeetingDateTime().plusWeeks(1).format(DATE_FORMATTER), responses.get(1).getMeetingDate());
        verify(meetingRepository).saveAll(anyList());
    }

    @Test
    void defineMeetingsForGroup_Success_DefaultTopicsFromCourseNameWhenFallbackTopicAndListAreNull() {
        DefineMeetingDTO dtoNoFallbackAndNoList = new DefineMeetingDTO(2, LocalDateTime.of(2024, 1, 1, 10, 0), null, null);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(courseGroup)).thenReturn(Optional.empty());
        when(meetingRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Meeting> meetings = invocation.getArgument(0);
            for (int i = 0; i < meetings.size(); i++) {
                meetings.get(i).setMeetingId((long) (i + 1));
            }
            return meetings;
        });

        List<MeetingResponse> responses = meetingService.defineMeetingsForGroup(1L, dtoNoFallbackAndNoList);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(course.getCourseName() + " - Spotkanie 1", responses.get(0).getTopic());
        assertEquals(course.getCourseName() + " - Spotkanie 2", responses.get(1).getTopic());
    }


    @Test
    void defineMeetingsForGroup_Success_ProvidedTopicsList() {
        DefineMeetingDTO dtoWithTopicsList = new DefineMeetingDTO(
                2,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                Arrays.asList("Topic A", "Topic B"),
                null
        );
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(courseGroup)).thenReturn(Optional.empty());
        when(meetingRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Meeting> meetings = invocation.getArgument(0);
            for (int i = 0; i < meetings.size(); i++) {
                meetings.get(i).setMeetingId((long) (i + 1));
            }
            return meetings;
        });

        List<MeetingResponse> responses = meetingService.defineMeetingsForGroup(1L, dtoWithTopicsList);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Topic A", responses.get(0).getTopic());
        assertEquals("Topic B", responses.get(1).getTopic());
    }

    @Test
    void defineMeetingsForGroup_Success_ProvidedTopicsList_SomeBlankUsesDefault() {
        DefineMeetingDTO dtoWithBlankInTopicsList = new DefineMeetingDTO(
                2,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                Arrays.asList("Topic A", "  "),
                null
        );
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(courseGroup)).thenReturn(Optional.empty());
        when(meetingRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Meeting> meetings = invocation.getArgument(0);
            for (int i = 0; i < meetings.size(); i++) {
                meetings.get(i).setMeetingId((long) (i + 1));
            }
            return meetings;
        });

        List<MeetingResponse> responses = meetingService.defineMeetingsForGroup(1L, dtoWithBlankInTopicsList);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Topic A", responses.get(0).getTopic());
        assertEquals(course.getCourseName() + " - Spotkanie 2", responses.get(1).getTopic());
    }


    @Test
    void defineMeetingsForGroup_GroupNotFound_ThrowsResourceNotFoundException() {
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> meetingService.defineMeetingsForGroup(1L, defineMeetingDTOWithoutTopicsList));
    }

    @Test
    void defineMeetingsForGroup_GroupMissingCourse_ThrowsIllegalStateException() {
        courseGroup.setCourse(null);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        assertThrows(IllegalStateException.class, () -> meetingService.defineMeetingsForGroup(1L, defineMeetingDTOWithoutTopicsList));
    }

    @Test
    void defineMeetingsForGroup_MismatchedTopicsAndMeetings_ThrowsInvalidOperationException() {
        DefineMeetingDTO mismatchedDto = new DefineMeetingDTO(
                2, LocalDateTime.now(), Collections.singletonList("Only One Topic"), null
        );
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        assertThrows(InvalidOperationException.class, () -> meetingService.defineMeetingsForGroup(1L, mismatchedDto));
    }

    @Test
    void defineMeetingsForGroup_ContinuesNumberingFromExistingMeetings() {
        Meeting existingMeeting = new Meeting();
        existingMeeting.setMeetingNumber(5);
        when(courseGroupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(courseGroup));
        when(meetingRepository.findTopByGroupOrderByMeetingNumberDesc(courseGroup)).thenReturn(Optional.of(existingMeeting));
        when(meetingRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Meeting> meetings = invocation.getArgument(0);
            for (int i = 0; i < meetings.size(); i++) {
                meetings.get(i).setMeetingId((long) (i + 1));
            }
            return meetings;
        });


        List<MeetingResponse> responses = meetingService.defineMeetingsForGroup(1L, defineMeetingDTOWithoutTopicsList);

        assertEquals(2, responses.size());
        assertEquals(6, responses.get(0).getMeetingNumber());
        assertEquals(7, responses.get(1).getMeetingNumber());
    }


    @Test
    void getMeetingsByGroupId_Success() {
        Meeting meeting1 = new Meeting(1L, 1, LocalDateTime.now(), "Topic 1", courseGroup, null);
        Meeting meeting2 = new Meeting(2L, 2, LocalDateTime.now().plusWeeks(1), "Topic 2", courseGroup, null);
        when(courseGroupRepository.existsById(1L)).thenReturn(true);
        when(courseGroupRepository.getReferenceById(1L)).thenReturn(courseGroup);
        when(meetingRepository.findByGroupOrderByMeetingNumber(courseGroup)).thenReturn(Arrays.asList(meeting1, meeting2));

        List<MeetingResponse> responses = meetingService.getMeetingsByGroupId(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void getMeetingsByGroupId_GroupNotFound_ThrowsResourceNotFoundException() {
        when(courseGroupRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> meetingService.getMeetingsByGroupId(1L));
    }
}