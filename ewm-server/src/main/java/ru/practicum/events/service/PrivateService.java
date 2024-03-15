package ru.practicum.events.service;

import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.requests.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateService {
    List<EventShortDto> getByPrivateList(Long userId, Integer from, Integer size);

    EventFullDto getByPrivate(Long userId, Long eventId);

    List<ParticipationRequestDto> getByPrivateRequests(Long userId, Long eventId);

    EventFullDto createByPrivate(NewEventDto eventDto, Long userId);

    EventFullDto updateByPrivate(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateByPrivateStatus(EventRequestStatusUpdateRequestDto updateRequest, Long userId,
                                                            Long eventId);
}
