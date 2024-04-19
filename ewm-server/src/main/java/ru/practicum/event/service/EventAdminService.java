package ru.practicum.event.service;

import ru.practicum.event.EventFullDto;
import ru.practicum.event.EventsParamsFiltre;
import ru.practicum.event.UpdateEventAdminRequest;

import java.util.List;

public interface EventAdminService {
    List<EventFullDto> getByAdmin(EventsParamsFiltre params);

    EventFullDto updateByAdmin(UpdateEventAdminRequest request, Long eventId);
}
