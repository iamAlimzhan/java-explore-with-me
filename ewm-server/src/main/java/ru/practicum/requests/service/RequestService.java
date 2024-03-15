package ru.practicum.requests.service;

import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto add(Long userId, Long eventId);

    List<ParticipationRequestDto> getAll(Long userId);

    ParticipationRequestDto undo(Long userId, Long requestId);
}
