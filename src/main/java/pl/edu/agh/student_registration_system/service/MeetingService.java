package pl.edu.agh.student_registration_system.service;

import jakarta.validation.Valid;
import pl.edu.agh.student_registration_system.payload.dto.DefineMeetingDTO;
import pl.edu.agh.student_registration_system.payload.response.MeetingResponse;

import java.util.List;

public interface MeetingService {
    List<MeetingResponse> defineMeetingsForGroup(Long groupId, @Valid DefineMeetingDTO defineMeetingDto);

    List<MeetingResponse> getMeetingsByGroupId(Long groupId);
}
