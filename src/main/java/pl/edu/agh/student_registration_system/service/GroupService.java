package pl.edu.agh.student_registration_system.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGroupDTO;
import pl.edu.agh.student_registration_system.payload.response.GroupAvailabilityResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.StudentResponse;

import java.util.List;

public interface GroupService {
    List<GroupResponse> getGroupsByCourseId(Long courseId);

    void deleteAllGroupsByCourseId(Long courseId);

    GroupResponse createGroup(@Valid CreateGroupDTO createGroupDto);

    GroupResponse getGroupResponseById(Long groupId);

    GroupResponse updateGroup(Long groupId, @Valid UpdateGroupDTO updateGroupDto);

    void deleteGroup(Long groupId);

    Page<GroupAvailabilityResponse> findAvailableGroupsForStudent(Long courseId, String search, Pageable pageable);

    Page<StudentResponse> getEnrolledStudents(Long groupId, Pageable pageable);
}
