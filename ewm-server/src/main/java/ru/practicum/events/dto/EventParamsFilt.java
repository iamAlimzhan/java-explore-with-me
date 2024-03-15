package ru.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.events.enums.SortedEvent;
import ru.practicum.events.enums.StateEvent;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventParamsFilt {
    private List<Long> ids;
    private List<StateEvent> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
    private String text;
    private Boolean paid;
    private Boolean onlyAvailable;
    private SortedEvent sort;
}
