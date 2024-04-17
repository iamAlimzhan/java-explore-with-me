package ru.practicum.event;

import lombok.*;
import ru.practicum.enums.EventSortEnum;
import ru.practicum.enums.StateEvent;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class EventFiltre {
    private EventSortEnum sort;
    private Integer from;
    private Integer size;
    private List<Long> ids;
    private List<StateEvent> states;
    private List<Long> categories;
    private Boolean paid;
    private Boolean onlyAvailable;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private String text;
}
