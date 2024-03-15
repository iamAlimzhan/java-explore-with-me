package ru.practicum.events.dto;

import lombok.*;
import ru.practicum.events.enums.SortedEvent;
import ru.practicum.events.enums.StateEvent;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventParamsFiltDto {
    private List<Long> ids = List.of();
    private List<StateEvent> states = List.of();
    private List<Long> categories = List.of();
    private String rangeStart;
    private String rangeEnd;
    private Integer from = 0;
    private Integer size = 10;
    private String text;
    private Boolean paid;
    private Boolean onlyAvailable = Boolean.FALSE;
    private SortedEvent sort = SortedEvent.EVENT_DATE;
}
