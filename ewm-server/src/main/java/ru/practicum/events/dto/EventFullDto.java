package ru.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.practicum.locations.dto.LocationDto;

@Getter
@AllArgsConstructor
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
