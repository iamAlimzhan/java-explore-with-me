package ru.practicum.request;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getList(Long userId);

    ParticipationRequestDto delete(Long userId, Long requestId);
}
