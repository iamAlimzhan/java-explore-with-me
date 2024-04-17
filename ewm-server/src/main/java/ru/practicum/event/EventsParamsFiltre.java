package ru.practicum.event;

import lombok.*;
import ru.practicum.enums.EventSortEnum;
import ru.practicum.enums.StateEvent;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventsParamsFiltre {
    private EventSortEnum sort = EventSortEnum.EVENT_DATE;
    private Integer from = 0;
    private Integer size = 10;
    private List<Long> ids = List.of();
    private List<StateEvent> states = List.of();
    private List<Long> categories = List.of();
    private Boolean paid;
    private Boolean onlyAvailable = Boolean.FALSE;
    private String rangeStart;
    private String rangeEnd;
    private String text;
}
