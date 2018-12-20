package example.googlecalendar.entities;

import lombok.Data;

@Data
public class EventDto {

    private String id;
    private String startTime;
    private String endTime;
    private String summary;
    private String description;
    private String location;
}
