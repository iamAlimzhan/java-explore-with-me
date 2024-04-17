package ru.practicum.event.service;

import ru.practicum.event.*;
import ru.practicum.request.EventRequestStatusUpdateRequest;
import ru.practicum.request.EventRequestStatusUpdateResult;
import ru.practicum.request.ParticipationRequestDto;

import java.util.List;

public interface EventPrivateService {
    List<EventShortDto> getPrivateList(Long userId, Integer from, Integer size);

    EventFullDto getPrivateEventDto(Long userId, Long eventId);

    List<ParticipationRequestDto> getPrivateRequestList(Long userId, Long eventId);

    EventFullDto createPrivateByEventDto(NewEventDto eventDto, Long userId);

    EventFullDto updatePrivateByEventDto(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    EventRequestStatusUpdateResult updatePrivateRequestStatus(EventRequestStatusUpdateRequest updateRequest, Long userId,
                                                              Long eventId);
}
