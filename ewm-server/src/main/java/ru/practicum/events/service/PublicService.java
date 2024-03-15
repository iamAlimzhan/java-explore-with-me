package ru.practicum.events.service;

import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventParamsFiltDto;
import ru.practicum.events.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PublicService {
    List<EventShortDto> getByPublicList(EventParamsFiltDto paramsDto, HttpServletRequest request);

    EventFullDto getByPublic(Long eventId, HttpServletRequest request);
}
