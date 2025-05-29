package pl.edu.agh.student_registration_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.student_registration_system.payload.dto.CreateGroupDTO;
import pl.edu.agh.student_registration_system.payload.dto.UpdateGroupDTO;
import pl.edu.agh.student_registration_system.payload.response.GroupAvailabilityResponse;
import pl.edu.agh.student_registration_system.payload.response.GroupResponse;
import pl.edu.agh.student_registration_system.payload.response.StudentResponse;
import pl.edu.agh.student_registration_system.service.GroupService;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<GroupResponse> addGroup(@Valid @RequestBody CreateGroupDTO createGroupDto) {
        GroupResponse newGroup = groupService.createGroup(createGroupDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroup);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF') or " +
            "(hasAuthority('STUDENT') and @groupSecurityService.isStudentEnrolledInGroup(#groupId)) or " +
            "(hasAuthority('TEACHER') and @groupSecurityService.isTeacherOfGroup(#groupId))")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long groupId) {
        GroupResponse group = groupService.getGroupResponseById(groupId);
        return ResponseEntity.ok(group);
    }

    @PatchMapping("/{groupId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable Long groupId, @Valid @RequestBody UpdateGroupDTO updateGroupDto) {
        GroupResponse updatedGroup = groupService.updateGroup(groupId, updateGroupDto);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAuthority('DEANERY_STAFF')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<Page<GroupAvailabilityResponse>> getAvailableGroups(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<GroupAvailabilityResponse> page = groupService.findAvailableGroupsForStudent(courseId, search, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{groupId}/students")
    @PreAuthorize("hasAuthority('DEANERY_STAFF') or (@groupSecurityService.isTeacherOfGroup(#groupId) and hasAuthority('TEACHER'))")
    public ResponseEntity<Page<StudentResponse>> getStudentsInGroup(
            @PathVariable Long groupId, Pageable pageable) {
        log.info("Fetching students");
        Page<StudentResponse> page = groupService.getEnrolledStudents(groupId, pageable);
        return ResponseEntity.ok(page);
    }
}