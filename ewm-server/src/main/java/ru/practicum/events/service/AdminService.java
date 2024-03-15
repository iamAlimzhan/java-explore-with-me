package ru.practicum.events.service;

import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventParamsFiltDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminService {
    List<EventFullDto> getByAdmin(EventParamsFiltDto params);

    EventFullDto updateByAdmin(UpdateEventAdminRequest request, Long eventId);
}
