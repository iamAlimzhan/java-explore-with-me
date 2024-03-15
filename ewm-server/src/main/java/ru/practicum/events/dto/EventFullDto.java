package ru.practicum.events.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.practicum.locations.dto.LocationDto;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class EventFullDto extends EventDto {
    private String createdOn;
    private String description;
    private LocationDto location;
    private Integer participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private String state;
}
