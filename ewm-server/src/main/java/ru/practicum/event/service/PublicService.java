package ru.practicum.event.service;

import ru.practicum.event.EventFullDto;
import ru.practicum.event.EventShortDto;
import ru.practicum.event.EventsParamsFiltre;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PublicService {
    List<EventShortDto> getListByPublic(EventsParamsFiltre paramsFiltre, HttpServletRequest httpServletRequest);

    EventFullDto getEventDtoByPublic(Long eventId, HttpServletRequest httpServletRequest);
}
