package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.enums.StatusRequest;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper mapper;

    @Override
    @Transactional
    public ParticipationRequestDto add(Long userId, Long eventId) {
        if (requestRepository.findFirstByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Запрос на участие уже существует");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Владелец события не разрешил создавать запрос к событию");
        }
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new ConflictException("Неверный статус события");
        }
        if (event.getParticipantLimit() > 0) {
            Long participants = requestRepository.countByEventIdAndStatus(event.getId(), StatusRequest.CONFIRMED);
            Long participantsLimit = event.getParticipantLimit();
            if (participants >= participantsLimit) {
                throw new ConflictException("Лимит участвников достиг");
            }
        }
        ParticipationRequest request = completeNewRequest(userId, event);
        return mapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> getAll(Long userId) {
        getUserIfExists(userId);
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return mapper.toRequestDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto undo(Long userId, Long requestId) {
        getUserIfExists(userId);
        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос участников не найдет"));
        request.setStatus(StatusRequest.CANCELED);
        return mapper.toRequestDto(requestRepository.save(request));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
    }

    private ParticipationRequest completeNewRequest(Long userId, Event event) {
        User user = getUserIfExists(userId);
        boolean needConfirmation = event.getRequestModeration();
        boolean hasParticipantsLimit = event.getParticipantLimit() != 0;
        StatusRequest status;

        if (needConfirmation && hasParticipantsLimit) {
            status = StatusRequest.PENDING;
        } else {
            status = StatusRequest.CONFIRMED;
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(user);
        participationRequest.setStatus(status);
        participationRequest.setEvent(event);

        return participationRequest;
    }

}
