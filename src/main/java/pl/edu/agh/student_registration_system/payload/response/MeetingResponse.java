package pl.edu.agh.student_registration_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {
    private Long meetingId;
    private Integer meetingNumber;
    private String meetingDate;
    private String topic;
    private Long groupId;
}